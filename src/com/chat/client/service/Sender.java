package com.chat.client.service;

import com.chat.client.ChatClient;
import com.chat.client.util.NetworkUtilities;
import com.chat.model.Message;
import com.chat.model.MessageTypes;
import com.chat.model.NodeInfo;
import com.chat.model.NoteCarrier;

import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Class [Sender] process the user input, translates the user input into [Message]
 * and sends them to the Chat Server
 *
 * @author sampath and manoj
 */
public class Sender extends Thread implements MessageTypes {

    Scanner userInput;
    String inputData = null;

    // flag indicating whether we have joined chat or not
    boolean hasJoined;

    // constructor
    public Sender() {
        userInput = new Scanner(System.in);
        hasJoined = false;
    }

    // thread entry point
    @Override
    public void run() {

        // until forever, unless the user enters SHUTDOWN or SHUTDOWN ALL
        while (true) {
            // get user input
            inputData = userInput.nextLine();

            if(inputData.startsWith("JOIN")) {
                // ignore if already joined
                if(hasJoined == true) {
                    System.err.println("You have already joined the chat");
                    continue;
                }

                // read server information user provided with JOIN command
                String[] connectionInfo = inputData.split("[ ]+");

                // if there is information, that may override the connectivity information
                // that was provided through properties
                try {
                    ChatClient.peerNodeInfo = new NodeInfo(connectionInfo[1], Integer.parseInt(connectionInfo[2]));
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {

                }

                // check if we have valid server information
                if(ChatClient.peerNodeInfo == null) {
                    System.err.println("[Sender].handleJoinCommand No peer connectivity information found");
                    continue;
                }

                // don't send request over the internet if client and peer are same
                if(!ChatClient.clientNodeInfo.equals(ChatClient.peerNodeInfo)) {
                    // send join request to the peer
                    NetworkUtilities.postDataToServer(new Message(JOIN, ChatClient.clientNodeInfo), ChatClient.peerNodeInfo);
                } else {
                    ChatClient.peerNodeInfo.setName(ChatClient.clientNodeInfo.getName());
                }

                // we are in
                hasJoined = true;
                System.out.println("Joined chat...");
            } else if(inputData.startsWith("LEAVE")) {
                // check if we are in the chat
                if(hasJoined == false) {
                    System.err.println("You have not joined a chat yet...");
                    continue;
                }

                // don't send request over the internet if client and peer are same
                if(!ChatClient.clientNodeInfo.equals(ChatClient.peerNodeInfo)) {
                    // send leave request to the successor, and continues till it find the node which has my NodeInfo as its successor
                    NetworkUtilities.postDataToServer(new Message(LEAVE, ChatClient.clientNodeInfo), ChatClient.peerNodeInfo);
                }

                // we are out
                hasJoined = false;
                System.out.println("Left chat..");

            } else if(inputData.startsWith("SHUTDOWN ALL")) {
                // check if we are in the chat
                if(hasJoined == false) {
                    System.err.println("To shutdown the whole chat, you need to first join the chat");
                    continue;
                }

                // don't send request over the internet if client and peer are same
                if(!ChatClient.clientNodeInfo.equals(ChatClient.peerNodeInfo)) {
                    // send shutdown all request to the successor, and continues till it find the node which has my NodeInfo as its successor
                    NetworkUtilities.postDataToServer(new Message(SHUTDOWN, ChatClient.clientNodeInfo), ChatClient.peerNodeInfo);
                }

                System.out.println("Sent shutdown all request...\n");
                System.out.println("Exiting...\n");
                // exit the system
                System.exit(0);
            } else if(inputData.startsWith("SHUTDOWN")) {
                // check if we are not in chat
                if(hasJoined == false) {
                    System.err.println("To shutdown the chat, you need to first join the chat");
                    continue;
                }

                // don't send request over the internet if client and peer are same
                if(!ChatClient.clientNodeInfo.equals(ChatClient.peerNodeInfo)) {
                    // send shutdown request to the successor, and continues till it find the node which has my NodeInfo as its successor
                    NetworkUtilities.postDataToServer(new Message(LEAVE, ChatClient.clientNodeInfo), ChatClient.peerNodeInfo);
                }

                System.out.println("Left the chat...");
                System.out.println("Exiting...\n");
                // exit the system
                System.exit(0);
            } else {
                // check if we are in the chat
                if(hasJoined == false) {
                    System.err.println("To send a note, you need to first join the chat");
                    continue;
                }

                // prepare NOTE
                String note = "Message from " + ChatClient.clientNodeInfo.getName() + "\n" + inputData;

                // don't send request over the internet if client and peer are same
                if(!ChatClient.clientNodeInfo.equals(ChatClient.peerNodeInfo)) {
                    NoteCarrier noteCarrier = new NoteCarrier(note, ChatClient.clientNodeInfo);
                    // send note request to the successor, and continues till it find the node which has my NodeInfo as its successor
                    NetworkUtilities.postDataToServer(new Message(NOTE, noteCarrier), ChatClient.peerNodeInfo);
                }
                System.out.println("Message sent...\n");
                System.out.println(note);
            }
        }
    }
}

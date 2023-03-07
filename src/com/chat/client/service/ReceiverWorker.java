package com.chat.client.service;

import com.chat.client.ChatClient;
import com.chat.client.util.NetworkUtilities;
import com.chat.model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CLass [ReceiverWorker] responsible to handles the requests coming from Server
 *
 * @author srinivas and manoj
 */
public class ReceiverWorker extends Thread implements MessageTypes {

    // logger for [ReceiverWorker] class
    public static final Logger LOGGER = Logger.getLogger(ReceiverWorker.class.getName());

    // Server Connection
    private Socket serverConnection;

    // object streams
    private ObjectInputStream readFromNet;
    private ObjectOutputStream writeToNet;

    // reference to Message object
    private Message message;

    // constructor
    public ReceiverWorker(Socket serverConnection) {
        this.serverConnection = serverConnection;
        try {
            // object streams
            readFromNet = new ObjectInputStream(this.serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(this.serverConnection.getOutputStream());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "could not open object streams", ex.getMessage());
        }
    }

    // thread code entry point
    @Override
    public void run() {
        try {
            // read message
            message = (Message) readFromNet.readObject();
            // close the server connection
            serverConnection.close();
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Message could not be read", ex.getMessage());
            // no use of get going
            System.exit(1);
        }

        // decide what to do depending on the type message received
        switch (message.getType())
        {
            case JOIN:
                // get new participant details
                NodeInfo joiningParticipantInfo = (NodeInfo) message.getContent();
                // get successor information
                NodeInfo previousSuccessor = ChatClient.peerNodeInfo;
                // send JOIN_EVENT to the new participant about new successor information
                NetworkUtilities.postDataToServer(new Message(JOIN_EVENT, previousSuccessor), joiningParticipantInfo);
                // update the new successor information
                ChatClient.peerNodeInfo = joiningParticipantInfo;
                // show who has joined
                System.out.println(joiningParticipantInfo.getName() + " joined in the Chat.");
                break;
            case JOIN_EVENT:
                // update new successor
                ChatClient.peerNodeInfo = (NodeInfo) message.getContent();
                System.out.println("Successor Information Updated: New Successor " + ChatClient.peerNodeInfo.getName());
                break;
            case LEAVE:
                // remove this participant's info
                NodeInfo leavingParticipantInfo = (NodeInfo) message.getContent();
                // if client and peer information are same, don't send request over the internet
                if(ChatClient.peerNodeInfo.equals(leavingParticipantInfo)) {
                    ChatClient.peerNodeInfo = ChatClient.clientNodeInfo;
                // otherwise send an LEAVE_EVENT to update the successor information along all the nodes in the network
                // till it finds the node which has successor details matching with leaving node details
                } else {
                    NodeInfoList nodeList = new NodeInfoList(ChatClient.clientNodeInfo, leavingParticipantInfo);
                    NetworkUtilities.postDataToServer(new Message(LEAVE_EVENT, nodeList), ChatClient.peerNodeInfo);
                }
                System.out.println(leavingParticipantInfo.getName() + " left the Chat");
                break;
            case LEAVE_EVENT:
                // leaving participant's info
                NodeInfoList leaveEventDetails = (NodeInfoList) message.getContent();
                leavingParticipantInfo = leaveEventDetails.getLeavingNode();
                // if successor information is matched with leaving node information
                // don't send request further, update the successor information
                if(ChatClient.peerNodeInfo.equals(leavingParticipantInfo)) {
                    ChatClient.peerNodeInfo = leaveEventDetails.getEventInitiator();
                } else {
                    // otherwise send request further to update the successor information and construct the ring again
                    NetworkUtilities.postDataToServer(new Message(LEAVE_EVENT, leaveEventDetails), ChatClient.peerNodeInfo);
                }
                System.out.println(leavingParticipantInfo.getName() + " left the Chat");
                System.out.println("Successor Information Updated: New Successor " + ChatClient.peerNodeInfo.getName());
                break;
            case SHUTDOWN:
                // leaving participant's info
                NodeInfo shutdownInitiator = (NodeInfo) message.getContent();
                // if successor information is matched with Shutdown Initiator node information
                // don't send request further, shutdown the node
                if(!ChatClient.peerNodeInfo.equals(shutdownInitiator)) {
                    NetworkUtilities.postDataToServer(new Message(SHUTDOWN, shutdownInitiator), ChatClient.peerNodeInfo);
                }
                System.out.println("Received shutdown message from " + shutdownInitiator.getName() + ", exiting...");
                System.exit(0);
                break;
            case NOTE:
                // node which sent the NOTE request
                NoteCarrier noteCarrier = (NoteCarrier) message.getContent();

                // print the note
                System.out.println(noteCarrier.getNote());
                // if successor information is matched with leaving node information
                // don't send request further, update the successor information
                if(!ChatClient.peerNodeInfo.equals(noteCarrier.getNoteInitiator())) {
                    NetworkUtilities.postDataToServer(new Message(NOTE, noteCarrier), ChatClient.peerNodeInfo);
                }
                break;
            default:
                LOGGER.log(Level.SEVERE, "Invalid Message Type");
        }
    }
}

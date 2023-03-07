package com.chat.client.util;

import com.chat.client.ChatClient;
import com.chat.client.service.Sender;
import com.chat.model.Message;
import com.chat.model.NodeInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Utility class for networking.
 */
public class NetworkUtilities {

    public static final Logger LOGGER = Logger.getLogger(NetworkUtilities.class.getName());

    /**
     * Helper class to retrieve ones own IPv4.
     * @see <a href="https://stackoverflow.com/questions/8083479/java-getting-my-ip-address">original source</a>
     * 
     * @return my own IPv4 address
     */
    public static String getMyIP() {
    try {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp() || networkInterface.isVirtual() || networkInterface.isPointToPoint()) {
                continue;
            }

            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();

                final String myIP = address.getHostAddress();
                if (Inet4Address.class == address.getClass()) {
                    return myIP;
                }
            }
        }
    } catch (SocketException e) {
        throw new RuntimeException(e);
    }
    return null;
    }

    /**
     *
     * @param message
     * @param peerNodeInfo
     *
     * @return response status about the request
     *
     * handles the communication between my node and peer nodes
     */
    public static boolean postDataToServer(Message message, NodeInfo peerNodeInfo) {
        // object streams
        Socket peerConnection;
        ObjectOutputStream writeToNet;
        ObjectInputStream readFromNet;
        try {
            // open connection to server
            peerConnection = new Socket(peerNodeInfo.getAddress(), peerNodeInfo.getPort());

            // open object streams
            writeToNet = new ObjectOutputStream(peerConnection.getOutputStream());
            readFromNet = new ObjectInputStream(peerConnection.getInputStream());

            // send request to the server
            writeToNet.writeObject(message);

            // close connection
            peerConnection.close();

            // flag to indicate that request is successful
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error Connecting to server or writing/reading object streams or closing connection",
                    ex.getMessage());
        }
        // flag to indicate that request is failed
        return false;
    }
}

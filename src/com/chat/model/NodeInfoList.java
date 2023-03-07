package com.chat.model;

import java.io.Serializable;

/**
 * class [NodeInfoList] which maintains Event Initiator Node and Leaving Node details
 *
 * @author manoj
 */
public class NodeInfoList implements Serializable {
    private NodeInfo eventInitiator;
    private NodeInfo leavingNode;

    public NodeInfoList(NodeInfo eventInitiator, NodeInfo leavingNode) {
        this.eventInitiator = eventInitiator;
        this.leavingNode = leavingNode;
    }

    // gets the event initiator information
    public NodeInfo getEventInitiator() {
        return eventInitiator;
    }

    public void setEventInitiator(NodeInfo eventInitiator) {
        this.eventInitiator = eventInitiator;
    }

    // gets the leaving node information
    public NodeInfo getLeavingNode() {
        return leavingNode;
    }

    public void setLeavingNode(NodeInfo leavingNode) {
        this.leavingNode = leavingNode;
    }
}
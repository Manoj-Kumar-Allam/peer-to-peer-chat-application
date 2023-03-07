package com.chat.model;

import java.io.Serializable;

/**
 * class [NoteCarrier] carrier the Note and the node which sends that request
 *
 * @author manoj
 */
public class NoteCarrier implements Serializable {

    private String note;

    private NodeInfo noteInitiator;

    public NoteCarrier(String note, NodeInfo noteInitiator) {
        this.note = note;
        this.noteInitiator = noteInitiator;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public NodeInfo getNoteInitiator() {
        return noteInitiator;
    }

    public void setNoteInitiator(NodeInfo noteInitiator) {
        this.noteInitiator = noteInitiator;
    }
}

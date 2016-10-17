package com.company;

import java.util.ArrayList;

/**
 * Created by Troy on 10/17/16.
 */
public class MessagesWrapper {
    ArrayList<Message> messages;

    public MessagesWrapper() {
    }

    public MessagesWrapper(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }
}

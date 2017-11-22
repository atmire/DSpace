package org.dspace.app.rest;

import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;

@Component
public class LogTopicListener implements MessageListener {

    @Override
    public void onMessage(Message message) {

        System.out.println(message);
    }
}

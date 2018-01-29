package org.dspace.log.appender;

import org.apache.log4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;

@Component
public class JMSListener implements MessageListener{
    private final Logger log = Logger.getLogger(JMSListener.class);

    @JmsListener(destination = MessagingConfiguration.QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void processMessage(String content) {
        log.error("Hier");
        System.out.println("hier");
        String t = content;
    }

    @JmsListener(destination = "ActiveMQ.Advisory.Consumer.Queue.testTopic", containerFactory = "jmsListenerContainerFactory")
    public void test(String test){
        log.error("test");
        System.out.println("test");
        String t = test;
    }

    public void onMessage(Message message) {
        log.error("OnMessage");
        log.error(message.toString());
        System.out.println("test");
        String t = "msg";
    }
}

package org.dspace.log.appender;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.dspace.app.rest.websocket.LogWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;

@Component
public class JMSListener{

    @Autowired
    private SimpMessagingTemplate webSocket;

    private final Logger log = Logger.getLogger(JMSListener.class);

    @JmsListener(destination = "newTest", containerFactory = "myFactory")
    public void processMessage(Object content) {
        ActiveMQObjectMessage msg = (ActiveMQObjectMessage) content;
        try {
            LoggingEvent loggingEvent = ((LoggingEvent) msg.getObject());
            String logContent = loggingEvent.getMessage().toString();
            Level level = loggingEvent.getLevel();
            log.info(logContent);
            webSocket.convertAndSend("/topic/entries", new LogWebSocketController.LogDTO(logContent, level));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

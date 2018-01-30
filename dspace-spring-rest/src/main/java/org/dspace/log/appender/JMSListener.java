package org.dspace.log.appender;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.util.ByteSequence;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;


@Component
public class JMSListener{
    private final Logger log = Logger.getLogger(JMSListener.class);

    @JmsListener(destination = "newTest", containerFactory = "myFactory")
    public void processMessage(Object content) {
        ActiveMQObjectMessage msg = (ActiveMQObjectMessage) content;
        try {
            LoggingEvent loggingEvent = ((LoggingEvent) msg.getObject());
            String logContent = loggingEvent.getMessage().toString();
            System.out.println(logContent);
            log.info(logContent);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

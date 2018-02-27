/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.log.appender;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.dspace.app.rest.websocket.LogWebSocketController;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.services.events.SystemEventService;
import org.dspace.usage.UsageEvent;
import org.dspace.websocket.stats.StatsEventWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;

@Component
public class JMSListener {

    @Autowired
    private SimpMessagingTemplate webSocket;

    @Autowired
    private UsageEventFromMessageHandler usageEventFromMessageHandler;

    private final Logger log = Logger.getLogger(JMSListener.class);

    @JmsListener(destination = "logTopic", containerFactory = "myFactory")
    public void processMessage(Object content) {
        ActiveMQObjectMessage msg = (ActiveMQObjectMessage) content;
        try {
            LoggingEvent loggingEvent = ((LoggingEvent) msg.getObject());
            String logContent = loggingEvent.getMessage().toString();
            Level level = loggingEvent.getLevel();
            log.info(logContent);
            webSocket.convertAndSend("/api/topics/logs", new LogWebSocketController.LogDTO(logContent, level));
        } catch (JMSException e) {
            log.error(e.getMessage());
        }
    }

    @JmsListener(destination = "eventTopic", containerFactory = "myFactory")
    public void processEventMessage(Object content) {
        ActiveMQObjectMessage msg = (ActiveMQObjectMessage) content;
        try {
            usageEventFromMessageHandler.processUsageEventFromMessage(msg);
        } catch (JMSException | SQLException e) {
            log.error(e.getMessage());
        }
    }



}

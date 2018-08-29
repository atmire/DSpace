package org.dspace.websocket.stats;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.dspace.app.rest.websocket.LogWebSocketController;
import org.dspace.log.appender.JMSListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/stats", encoders = {StatsEventWrapperEncoder.class}, decoders = {StatsEventWrapperDecoder.class})
public class StatsWebSocket {

    private final Logger log = Logger.getLogger(StatsWebSocket.class);
    javax.jms.Session jmsSession;
    MessageProducer messageProducer;

    @Autowired
    ConnectionFactory cf;

    public StatsWebSocket(){
        Connection connection = null;
        try {
            connection = cf.createConnection();
            jmsSession = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
            Destination destination = jmsSession.createTopic("eventTopic");
            messageProducer = jmsSession.createProducer(destination);
        } catch (JMSException e) {
            log.error(e.getMessage());
        }
    }
    @OnOpen
    public void onOpen(Session session) throws IOException {
        log.debug("Stats WebSocket Opened");
    }

    @OnMessage
    public void onMessage(Session session, StatsEventWrapper message) throws IOException {
        log.debug("Stats WebSocket OnMessage");
        try {
            ObjectMessage jmsMessage = jmsSession.createObjectMessage();
            jmsMessage.setObject(message);
            messageProducer.send(jmsMessage);
        } catch (JMSException e){
            log.error(e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        log.debug("Stats WebSocket OnClose");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.debug("Stats WebSocket OnError");

    }
}

package org.dspace.websocket.stats;

import org.apache.log4j.Logger;
import org.dspace.app.rest.websocket.LogWebSocketController;
import org.dspace.log.appender.JMSListener;

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

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Get session and WebSocket connection
        log.info("It opened");
    }

    @OnMessage
    public void onMessage(Session session, StatsEventWrapper message) throws IOException {
        // Handle new messages
        //TODO Send to JMS topic
        log.info("on message");
        log.info(message);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
        log.info(" on close ");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        log.info("on error");

    }
}

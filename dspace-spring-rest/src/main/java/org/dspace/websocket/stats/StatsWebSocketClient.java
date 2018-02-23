package org.dspace.websocket.stats;

import org.apache.log4j.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint(encoders = {StatsEventWrapperEncoder.class}, decoders = {StatsEventWrapperDecoder.class})
public class StatsWebSocketClient {

    private final String uri = "ws://127.0.0.1:8080/dspace7-rest/stats";
    private Session session;
    private final Logger log = Logger.getLogger(StatsWebSocketClient.class);

    public StatsWebSocketClient(){
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, new URI(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session){
        this.session=session;
    }

    @OnMessage
    public void onMessage(String message, Session session){
        log.info("TESTING");
    }

    public void sendMessage(String message){
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException ex) {
            log.error(ex);
        }
    }

    public void sendMessage(StatsEventWrapper statsEventWrapper){
        try {
            session.getBasicRemote().sendObject(statsEventWrapper);

        } catch (Exception ex) {
            log.error(ex);
        }
    }
}

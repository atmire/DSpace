package org.dspace.websocket.stats;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.dspace.log.appender.JMSListener;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class StatsEventWrapperEncoder implements Encoder.Text<StatsEventWrapper> {

    private final Logger log = Logger.getLogger(StatsEventWrapperEncoder.class);

    public String encode(StatsEventWrapper statsEventWrapper) throws EncodeException {
        return new Gson().toJson(statsEventWrapper);
    }

    public void init(EndpointConfig endpointConfig) {
        log.info("init encoder");
    }

    public void destroy() {
        log.info("destroy encoder");
    }
}

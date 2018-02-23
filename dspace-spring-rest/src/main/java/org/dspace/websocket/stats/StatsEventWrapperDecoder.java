package org.dspace.websocket.stats;

import com.google.gson.Gson;
import org.apache.log4j.Logger;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class StatsEventWrapperDecoder implements Decoder.Text<StatsEventWrapper>{

    private final Logger log = Logger.getLogger(StatsEventWrapperEncoder.class);


    public StatsEventWrapper decode(String s) throws DecodeException {
        return new Gson().fromJson(s, StatsEventWrapper.class);
    }

    public boolean willDecode(String s) {
        return true;
    }

    public void init(EndpointConfig endpointConfig) {
        log.info("init decoder");
    }

    public void destroy() {
        log.info("destroy decoder");
    }
}

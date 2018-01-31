package org.dspace.app.rest.websocket;

import org.apache.log4j.Level;
import org.springframework.stereotype.Controller;

import java.util.Calendar;
import java.util.Date;

@Controller
public class LogWebSocketController {
    /**
     * A simple DTO class to encapsulate messages along with their timestamps.
     */
    public static class LogDTO {
        public Date date;
        public String content;
        public String level;
        public LogDTO(String content, Level level) {
            this.date = Calendar.getInstance().getTime();
            this.level = level.toString();
            this.content = content;
        }
    }
}

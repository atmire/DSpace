/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
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

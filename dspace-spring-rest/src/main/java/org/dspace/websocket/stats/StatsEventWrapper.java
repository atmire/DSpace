package org.dspace.websocket.stats;

import org.apache.log4j.Logger;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.usage.UsageEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.UUID;

public class StatsEventWrapper {

    private transient HttpServletRequest request;
    private transient final Logger log = Logger.getLogger(StatsEventWrapper.class);
    private transient UsageEvent event;

    private String uuid;

    public StatsEventWrapper(UsageEvent.Action action, HttpServletRequest request, String uuid){
        Context ctx = new Context();
        try {
            //TODO Generalize which service to use
            UsageEvent usageEvent = new UsageEvent(action, request, ctx, ContentServiceFactory.getInstance().getItemService().find(ctx, UUID.fromString(uuid)));
            this.event = usageEvent;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        this.request = request;
        this.uuid = uuid;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public UsageEvent getEvent() {
        return event;
    }

    public void setEvent(UsageEvent event) {
        this.event = event;
    }
}

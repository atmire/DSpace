package org.dspace.log.appender;

import java.sql.SQLException;
import java.util.UUID;
import javax.jms.JMSException;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.services.events.SystemEventService;
import org.dspace.usage.UsageEvent;
import org.dspace.websocket.stats.StatsEventWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsageEventFromMessageHandler {

    @Autowired
    private SystemEventService systemEventService;

    public void processUsageEventFromMessage(ActiveMQObjectMessage msg) throws JMSException, SQLException {
        StatsEventWrapper statsEventWrapper = (StatsEventWrapper) msg.getObject();
        //TODO this service needs to be generalized via the typegiven in the StatsEventWrapper
        ItemService itemService = ContentServiceFactory.getInstance().getItemService();
        Context context = new Context();
        context.setCurrentUser(statsEventWrapper.getCurrentUser());
        UsageEvent usageEvent = new UsageEvent(UsageEvent.Action.valueOf(statsEventWrapper.getAction().toUpperCase()), statsEventWrapper.getIpAdress(), statsEventWrapper.getUserAgent(), statsEventWrapper.getxForwardedFor(), context, itemService.find(context, UUID.fromString(statsEventWrapper.getUuid())));
        systemEventService.fireEvent(usageEvent);
    }
}

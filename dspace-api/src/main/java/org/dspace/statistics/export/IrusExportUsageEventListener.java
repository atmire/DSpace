/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics.export;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import javax.annotation.Resource;

import org.apache.logging.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.services.ConfigurationService;
import org.dspace.services.model.Event;
import org.dspace.statistics.export.processor.BitstreamEventProcessor;
import org.dspace.statistics.export.processor.ItemEventProcessor;
import org.dspace.usage.AbstractUsageEventListener;
import org.dspace.usage.UsageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

/**
 * Class to receive usage events and send corresponding data to IRUS
 */
public class IrusExportUsageEventListener extends AbstractUsageEventListener {
    /*  Log4j logger*/
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    @Autowired
    ConfigurationService configurationService;

    @Resource(name = "dspaceRunnableThreadExecutor")
    TaskExecutor taskExecutor;

    /**
     * Receives an event and processes to create a URL to send to IRUS when certain conditions are met
     *
     * @param event includes all the information related to the event that occurred
     */
    @Override
    public void receiveEvent(Event event) {
        if (configurationService.getBooleanProperty("irus.statistics.tracker.enabled", false)) {
            if (event instanceof UsageEvent) {
                UsageEvent ue = (UsageEvent) event;
                Context context = ue.getContext();

                try {
                    //Check for item investigation
                    if (ue.getObject() instanceof Item) {
                        ItemEventProcessor itemEventProcessor = new ItemEventProcessor(context, ue.getRequest(),
                                                                                       (Item) ue.getObject());
                        taskExecutor.execute(() -> {
                            try {
                                itemEventProcessor.processEvent();
                            } catch (SQLException | IOException e) {
                                handleException(e, ue);
                            }
                        });
                    } else if (ue.getObject() instanceof Bitstream) {

                        BitstreamEventProcessor bitstreamEventProcessor =
                                new BitstreamEventProcessor(context, ue.getRequest(), (Bitstream) ue.getObject());
                        taskExecutor.execute(() -> {
                            try {
                                bitstreamEventProcessor.processEvent();
                            } catch (SQLException | IOException e) {
                                handleException(e, ue);
                            }
                        });
                    }
                } catch (Exception e) {
                    handleException(e, ue);
                }
            }
        }
    }

    private void handleException(Exception e, UsageEvent ue) {
        UUID id;
        id = ue.getObject().getID();

        int type;
        try {
            type = ue.getObject().getType();
        } catch (Exception e1) {
            type = -1;
        }
        log.error(LogManager.getHeader(ue.getContext(), "Error while processing export of use event",
                                       "Id: " + id + " type: " + type), e);
    }
}

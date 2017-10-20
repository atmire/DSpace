package org.dspace.log.appender;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.log4j.net.JMSAppender;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Extended JMS log appender to also log the web app and server node name
 */
public class EnhancedJMSAppender extends JMSAppender {

    private String nodeName;
    private String webapp = "TODO";

    /**
     * Method enriches the headers so that JMS consumers know who you are
     */
    @Override
    public void append(LoggingEvent event) {
        if(!checkEntryConditions()) {
            return;
        }

        try {
            ActiveMQConnection activeMqConnection = (ActiveMQConnection)getTopicConnection();

            if (!activeMqConnection.isStarted()) {
                activeMqConnection.start();
            }

            ObjectMessage msg = getTopicSession().createObjectMessage();
            if (getLocationInfo()) {
                event.getLocationInformation();
            }
            msg.setStringProperty("node.name", nodeName);
            msg.setStringProperty("webapp.name", webapp);
            msg.setStringProperty("hostname", InetAddress.getLocalHost().getHostName());
            msg.setStringProperty("ip-address", InetAddress.getLocalHost().getHostAddress());
            msg.setObject(event);

            getTopicPublisher().publish(msg);

        } catch(JMSException e) {
            errorHandler.error("Could not publish message in JMSAppender ["+name+"].", e,
                    ErrorCode.GENERIC_FAILURE);
        } catch(RuntimeException e) {
            errorHandler.error("Could not publish message in JMSAppender ["+name+"].", e,
                    ErrorCode.GENERIC_FAILURE);
        } catch (UnknownHostException e) {
            errorHandler.error("Could not publish message in JMSAppender ["+name+"].", e,
                    ErrorCode.GENERIC_FAILURE);
        }
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

}

package org.dspace.websocket.stats;

import org.dspace.eperson.EPerson;

import java.io.Serializable;

public class StatsEventWrapper implements Serializable {

    private String xForwardedFor;
    private String userAgent;
    private String uuid;
    private String action;
    private String ipAddress;
    private String type;
    private EPerson currentUser;

    public StatsEventWrapper(){
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getxForwardedFor() {
        return xForwardedFor;
    }

    public void setxForwardedFor(String xForwardedFor) {
        this.xForwardedFor = xForwardedFor;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public EPerson getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(EPerson currentUser) {
        this.currentUser = currentUser;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

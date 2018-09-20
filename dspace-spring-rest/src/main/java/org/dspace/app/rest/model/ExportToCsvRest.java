package org.dspace.app.rest.model;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.dspace.app.rest.ExportToCsvRestController;

public class ExportToCsvRest implements RestAddressableModel {

    @JsonProperty("dso-id")
    private UUID dsoUuid;
    private Date date;
    private String state;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long size = null;

    private String category;
    private String type;

    public UUID getDsoUuid() {
        return dsoUuid;
    }

    public void setDsoUuid(UUID dsoUuid) {
        this.dsoUuid = dsoUuid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getCategory() {
        return category;
    }

    public Class getController() {
        return ExportToCsvRestController.class;
    }

    public String getType() {
        return type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setType(String type) {
        this.type = type;
    }
}
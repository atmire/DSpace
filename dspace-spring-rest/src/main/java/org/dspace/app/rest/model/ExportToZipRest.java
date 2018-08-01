package org.dspace.app.rest.model;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dspace.app.rest.ExportToZipRestController;

public class ExportToZipRest implements RestAddressableModel {

    @JsonProperty("collection-id")
    private UUID collectionId;
    private Date date;
    private String state;
    private int size;

    public UUID getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(UUID collectionId) {
        this.collectionId = collectionId;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getCategory() {
        return "category";
    }

    public Class getController() {
        return ExportToZipRestController.class;
    }

    public String getType() {
        return "exportToZip";
    }
}

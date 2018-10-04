package org.dspace.app.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.dspace.app.rest.MappingItemRestController;

public class MappingItemRestWrapper implements RestAddressableModel {

    @JsonIgnore
    private List<ItemRest> mappingItemRestList;

    public List<ItemRest> getMappingItemRestList() {
        return mappingItemRestList;
    }

    public void setMappingItemRestList(List<ItemRest> mappingItemRestList) {
        this.mappingItemRestList = mappingItemRestList;
    }

    public String getCategory() {
        return "core";
    }

    public Class getController() {
        return MappingItemRestController.class;
    }

    public String getType() {
        return "collection";
    }
}

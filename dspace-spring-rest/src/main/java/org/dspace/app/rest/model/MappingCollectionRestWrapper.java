package org.dspace.app.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.dspace.app.rest.MappingCollectionRestController;

public class MappingCollectionRestWrapper implements RestAddressableModel {

    @JsonIgnore
    private List<CollectionRest> mappingCollectionRestList;

    public List<CollectionRest> getMappingCollectionRestList() {
        return mappingCollectionRestList;
    }

    public void setMappingCollectionRestList(List<CollectionRest> mappingCollectionRestList) {
        this.mappingCollectionRestList = mappingCollectionRestList;
    }

    public String getCategory() {
        return "core";
    }

    public Class getController() {
        return MappingCollectionRestController.class;
    }

    public String getType() {
        return "collection";
    }

}

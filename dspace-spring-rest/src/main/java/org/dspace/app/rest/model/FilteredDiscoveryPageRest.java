package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dspace.app.rest.RestResourceController;

public class FilteredDiscoveryPageRest extends BaseObjectRest<String> {

    public static final String NAME = "filtered-discovery-page";
    public static final String CATEGORY = "config";

    public String getCategory() {
        return CATEGORY;
    }

    public Class getController() {
        return RestResourceController.class;
    }

    public String getType() {
        return NAME;
    }

    @JsonProperty(value = "filter-name")
    private String label;
    @JsonProperty(value = "discovery-query")
    private String filterQueryString;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setFilterQueryString(String filterQueryString) {
        this.filterQueryString = filterQueryString; }

    public String getFilterQueryString() {
        return this.filterQueryString; }
}

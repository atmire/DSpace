package org.dspace.app.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.dspace.app.rest.ExportToCsvRestController;
import org.dspace.content.DSpaceObject;

public class ExportToCsvRestWrapper implements RestAddressableModel {

    @JsonIgnore
    private List<ExportToCsvRest> exportToCsvRestList;

    @JsonIgnore
    private DSpaceObject itemToBeExported;

    public List<ExportToCsvRest> getExportToCsvRestList() {
        return exportToCsvRestList;
    }

    public void setExportToCsvRestList(List<ExportToCsvRest> exportToCsvRestList) {
        this.exportToCsvRestList = exportToCsvRestList;
    }

    public String getCategory() {
        return "category";
    }

    public Class getController() {
        return ExportToCsvRestController.class;
    }

    public String getType() {
        return "exportToCsv";
    }

    public DSpaceObject getItemToBeExported() {
        return itemToBeExported;
    }

    public void setItemToBeExported(DSpaceObject itemToBeExported) {
        this.itemToBeExported = itemToBeExported;
    }
}
package org.dspace.app.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.dspace.app.rest.ExportToZipRestController;
import org.dspace.content.DSpaceObject;

public class ExportToZipRestWrapper implements RestAddressableModel {

    @JsonIgnore
    private List<ExportToZipRest> exportToZipRestList;

    @JsonIgnore
    private DSpaceObject itemToBeExported;

    public List<ExportToZipRest> getExportToZipRestList() {
        return exportToZipRestList;
    }

    public void setExportToZipRestList(List<ExportToZipRest> exportToZipRestList) {
        this.exportToZipRestList = exportToZipRestList;
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

    public DSpaceObject getItemToBeExported() {
        return itemToBeExported;
    }

    public void setItemToBeExported(DSpaceObject itemToBeExported) {
        this.itemToBeExported = itemToBeExported;
    }
}

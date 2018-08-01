package org.dspace.app.rest.converter;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.app.rest.model.ExportToZipRest;
import org.dspace.content.ExportToZip;
import org.dspace.content.service.CollectionService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExportToZipConverter
    extends DSpaceConverter<org.dspace.content.ExportToZip, ExportToZipRest> {

    private static final Logger log = Logger.getLogger(ExportToZipConverter.class);

    @Autowired
    CollectionService collectionService;

    @Override
    public ExportToZipRest fromModel(ExportToZip obj) {
        ExportToZipRest exportToZipRest = new ExportToZipRest();
        exportToZipRest.setCollectionId(obj.getDso().getID());
        exportToZipRest.setDate(obj.getDate());
        //TODO Change
        exportToZipRest.setSize(1234);
        exportToZipRest.setState(obj.getStatus());
        return exportToZipRest;
    }

    @Override
    public ExportToZip toModel(ExportToZipRest obj) {
        try {
            ExportToZip exportToZip = new ExportToZip();
            exportToZip.setStatus(obj.getState());
            exportToZip.setDso(collectionService.find(new Context(), obj.getCollectionId()));
            exportToZip.setDate(obj.getDate());
            return exportToZip;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}

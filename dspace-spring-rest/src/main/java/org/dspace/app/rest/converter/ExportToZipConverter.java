package org.dspace.app.rest.converter;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.app.rest.model.ExportToZipRest;
import org.dspace.content.Bitstream;
import org.dspace.content.ExportToZip;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.CollectionService;
import org.dspace.core.Context;
import org.dspace.export.ExportStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExportToZipConverter
    extends DSpaceConverter<org.dspace.content.ExportToZip, ExportToZipRest> {

    private static final Logger log = Logger.getLogger(ExportToZipConverter.class);

    @Autowired
    CollectionService collectionService;

    @Autowired
    BitstreamService bitstreamService;

    @Override
    public ExportToZipRest fromModel(ExportToZip obj) {
        ExportToZipRest exportToZipRest = new ExportToZipRest();
        exportToZipRest.setDsoUuid(obj.getDso().getID());
        exportToZipRest.setDate(obj.getDate());

        if (obj.getStatus().equals(ExportStatus.COMPLETED)) {
            Bitstream linkedBitstream = null;

            try {
                linkedBitstream = bitstreamService.find(new Context(), obj.getBitstreamId());
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            exportToZipRest.setSize(linkedBitstream.getSizeBytes());
        }
        exportToZipRest.setState(obj.getStatus());
        return exportToZipRest;
    }

    public ExportToZipRest fromModel(ExportToZip obj, String model, String apiCategory) {
        ExportToZipRest exportToZipRest = fromModel(obj);
        exportToZipRest.setCategory(apiCategory);
        exportToZipRest.setType(model);
        return exportToZipRest;
    }

    @Override
    public ExportToZip toModel(ExportToZipRest obj) {
        try {
            ExportToZip exportToZip = new ExportToZip();
            exportToZip.setStatus(obj.getState());
            exportToZip.setDso(collectionService.find(new Context(), obj.getDsoUuid()));
            exportToZip.setDate(obj.getDate());
            return exportToZip;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}

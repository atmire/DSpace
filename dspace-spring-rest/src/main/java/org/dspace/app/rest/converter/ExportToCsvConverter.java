package org.dspace.app.rest.converter;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.app.rest.model.ExportToCsvRest;
import org.dspace.content.Bitstream;
import org.dspace.content.ExportToCsv;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.CollectionService;
import org.dspace.core.Context;
import org.dspace.export.ExportStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExportToCsvConverter
    extends DSpaceConverter<org.dspace.content.ExportToCsv, ExportToCsvRest> {

    private static final Logger log = Logger.getLogger(ExportToCsvConverter.class);

    @Autowired
    CollectionService collectionService;

    @Autowired
    BitstreamService bitstreamService;

    @Override
    public ExportToCsvRest fromModel(ExportToCsv obj) {
        ExportToCsvRest exportToCsvRest = new ExportToCsvRest();
        exportToCsvRest.setDsoUuid(obj.getDso().getID());
        exportToCsvRest.setDate(obj.getDate());

        if (obj.getStatus().equals(ExportStatus.COMPLETED)) {
            Bitstream linkedBitstream = null;

            try {
                linkedBitstream = bitstreamService.findByIdOrLegacyId(new Context(), obj.getBitstreamId().toString());
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            exportToCsvRest.setSize(linkedBitstream.getSizeBytes());
        }
        exportToCsvRest.setState(obj.getStatus());
        return exportToCsvRest;
    }

    public ExportToCsvRest fromModel(ExportToCsv obj, String model, String apiCategory) {
        ExportToCsvRest exportToCsvRest = fromModel(obj);
        exportToCsvRest.setCategory(apiCategory);
        exportToCsvRest.setType(model);
        return exportToCsvRest;
    }

    @Override
    public ExportToCsv toModel(ExportToCsvRest obj) {
        try {
            ExportToCsv exportToCsv = new ExportToCsv();
            exportToCsv.setStatus(obj.getState());
            exportToCsv.setDso(collectionService.find(new Context(), obj.getDsoUuid()));
            exportToCsv.setDate(obj.getDate());
            return exportToCsv;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
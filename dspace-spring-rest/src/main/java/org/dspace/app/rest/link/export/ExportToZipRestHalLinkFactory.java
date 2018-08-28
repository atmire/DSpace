package org.dspace.app.rest.link.export;

import org.dspace.app.rest.ExportToZipRestController;
import org.dspace.app.rest.link.HalLinkFactory;

public abstract class ExportToZipRestHalLinkFactory<T> extends HalLinkFactory<T, ExportToZipRestController> {


    @Override
    protected Class<ExportToZipRestController> getControllerClass() {
        return ExportToZipRestController.class;
    }
}

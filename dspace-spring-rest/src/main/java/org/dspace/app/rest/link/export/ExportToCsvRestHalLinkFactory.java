package org.dspace.app.rest.link.export;

import org.dspace.app.rest.ExportToCsvRestController;
import org.dspace.app.rest.link.HalLinkFactory;

public abstract class ExportToCsvRestHalLinkFactory<T> extends HalLinkFactory<T, ExportToCsvRestController> {


    @Override
    protected Class<ExportToCsvRestController> getControllerClass() {
        return ExportToCsvRestController.class;
    }
}
package org.dspace.app.rest.link;

import org.dspace.app.rest.MappingItemRestController;

public abstract class MappingItemRestHalLinkFactory<T> extends HalLinkFactory<T, MappingItemRestController> {

    @Override
    protected Class<MappingItemRestController> getControllerClass() {
        return MappingItemRestController.class;
    }
}

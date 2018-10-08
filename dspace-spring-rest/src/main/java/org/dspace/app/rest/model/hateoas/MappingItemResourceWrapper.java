package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.MappingItemRestWrapper;
import org.dspace.app.rest.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

public class MappingItemResourceWrapper extends HALResource<MappingItemRestWrapper> {
    private static final Logger log = LoggerFactory.getLogger(MappingItemResourceWrapper.class);

    public MappingItemResourceWrapper(MappingItemRestWrapper content, Utils utils, Pageable pageable, String... rels) {
        super(content);
    }
}

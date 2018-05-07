/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.dspace.app.rest.converter.EPersonConverter;
import org.dspace.app.rest.model.EPersonRest;
import org.dspace.app.rest.model.hateoas.EPersonResource;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * This is the repository responsible to manage EPerson Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(EPersonRest.CATEGORY + "." + EPersonRest.NAME)
public class EPersonRestRepository extends DSpaceRestRepository<EPersonRest, UUID> {
    EPersonService es = EPersonServiceFactory.getInstance().getEPersonService();

    @Autowired
    EPersonConverter converter;

    @Override
    @PreAuthorize("hasPermission(#id, 'EPERSON', 'READ')")
    public EPersonRest findOne(UUID id) {
        EPerson eperson = null;
        try {
            eperson = es.find(obtainContext(), id);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (eperson == null) {
            throw new ResourceNotFoundException();
        }
        return converter.fromModel(eperson);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<EPersonRest> findAll(Pageable pageable) {
        List<EPerson> epersons = null;
        Context context = obtainContext();
        int total = 0;
        try {
            total = es.countTotal(context);
            epersons = es.findAll(context, EPerson.EMAIL, pageable.getPageSize(), pageable.getOffset());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Page<EPersonRest> page = new PageImpl<EPerson>(epersons, pageable, total).map(converter);
        return page;
    }

    @Override
    public Class<EPersonRest> getDomainClass() {
        return EPersonRest.class;
    }

    @Override
    public EPersonResource wrapResource(EPersonRest eperson, String... rels) {
        return new EPersonResource(eperson, utils, rels);
    }

}
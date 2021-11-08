/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.AbstractResource;

/**
 * This class acts as a {@link AbstractResource} used by Spring's framework to send the data in a proper and
 * streamlined way inside the {@link org.springframework.http.ResponseEntity} body.
 * This class' attributes are being used by Spring's framework in the overridden methods so that the proper
 * attributes are given and used in the response.
 */
public class BitstreamResource extends AbstractResource {

    private Bitstream bitstream;
    private String name;
    private UUID uuid;
    private long sizeBytes;
    private UUID currentUserUUID;

    @Autowired
    private BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();

    @Autowired
    private EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();

    public BitstreamResource(Bitstream bitstream, String name, UUID uuid, long sizeBytes, UUID currentUserUUID) {
        this.bitstream = bitstream;
        this.name = name;
        this.uuid = uuid;
        this.sizeBytes = sizeBytes;
        this.currentUserUUID = currentUserUUID;
    }

    @Override
    public String getDescription() {
        return "bitstream [" + uuid + "]";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        Context context = new Context();
        try {
            EPerson currentUser = ePersonService.find(context, currentUserUUID);
            context.setCurrentUser(currentUser);
            return bitstreamService.retrieve(context, bitstream);
        } catch (SQLException|AuthorizeException throwables) {
           throw new IOException(throwables);

        } finally {
            try {
                context.complete();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }

    }

    @Override
    public String getFilename() {
        return name;
    }

    @Override
    public long contentLength() throws IOException {
        return sizeBytes;
    }
}

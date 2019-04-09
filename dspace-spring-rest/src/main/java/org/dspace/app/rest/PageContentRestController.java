/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.model.PageRest;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.MultipartFileSender;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Context;
import org.dspace.pages.Page;
import org.dspace.pages.service.PageService;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/" + PageRest.CATEGORY + "/" + PageRest.PLURAL_NAME
    + "/{uuid:[0-9a-fxA-FX]{8}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{12}}/content")
public class PageContentRestController {

    //Most file systems are configured to use block sizes of 4096 or 8192 and our buffer should be a multiple of that.
    private static final int BUFFER_SIZE = 4096 * 10;

    private static final Logger log = org.apache.logging.log4j.LogManager
        .getLogger(PageContentRestController.class);

    @Autowired
    private PageService pageService;

    @Autowired
    private BitstreamService bitstreamService;

    @Autowired
    private ConfigurationService configurationService;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public void retrieve(@PathVariable UUID uuid, HttpServletResponse response,
                         HttpServletRequest request) throws IOException, SQLException, AuthorizeException {


        Context context = ContextUtil.obtainContext(request);

        Page page = pageService.findByUuid(context, uuid);
        if (page == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Bitstream bitstream = page.getBitstream();
        if (bitstream != null) {
            // Pipe the bits
            try (InputStream is = bitstreamService.retrieve(context, bitstream)) {
                MultipartFileSender sender = MultipartFileSender
                    .fromInputStream(is)
                    .withBufferSize(BUFFER_SIZE)
                    .withFileName(page.getName())
                    .withLength(bitstream.getSizeBytes())
                    .withChecksum(bitstream.getChecksum())
                    .withMimetype("html")
                    .withLastModified(bitstreamService.getLastModified(bitstream))
                    .with(request)
                    .with(response);

                //Determine if we need to send the file as a download or if the browser can open it inline
                long dispositionThreshold = configurationService.getLongProperty("webui.content_disposition_threshold");
                if (dispositionThreshold >= 0 && bitstream.getSizeBytes() > dispositionThreshold) {
                    sender.withDisposition(MultipartFileSender.CONTENT_DISPOSITION_ATTACHMENT);
                }

                //We have all the data we need, close the connection to the database so that it doesn't stay open during
                //download/streaming
                context.complete();

                //Send the data
                if (sender.isValid()) {
                    sender.serveResource();
                }

            } catch (ClientAbortException ex) {
                log.debug("Client aborted the request before the download was completed. " +
                              "Client is probably switching to a Range request.", ex);
            }

        }
    }
}

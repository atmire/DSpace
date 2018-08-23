package org.dspace.app.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.converter.ExportToZipConverter;
import org.dspace.app.rest.link.HalLinkService;
import org.dspace.app.rest.model.CollectionRest;
import org.dspace.app.rest.model.ExportToZipRest;
import org.dspace.app.rest.model.ExportToZipRestWrapper;
import org.dspace.app.rest.model.hateoas.ExportToZipResource;
import org.dspace.app.rest.model.hateoas.ExportToZipResourceWrapper;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.MultipartFileSender;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.ExportToZip;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ExportToZipService;
import org.dspace.core.Context;
import org.dspace.export.ExportToZipTask;
import org.dspace.services.ConfigurationService;
import org.dspace.services.EventService;
import org.dspace.usage.UsageEvent;
import org.dspace.utils.DSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/" + CollectionRest.CATEGORY + "/" + CollectionRest.PLURAL_NAME
    + "/{uuid:[0-9a-fxA-FX]{8}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{12}}/exportToZip")
public class ExportToZipRestController {

    //Most file systems are configured to use block sizes of 4096 or 8192 and our buffer should be a multiple of that.
    private static final int BUFFER_SIZE = 4096 * 10;

    @Autowired
    ExportToZipConverter exportToZipConverter;

    @Autowired
    CollectionService collectionService;

    @Autowired
    ExportToZipService exportToZipService;

    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    private EventService eventService;

    @Autowired
    protected Utils utils;

    @Autowired
    private HalLinkService halLinkService;

    @Autowired
    private ConfigurationService configurationService;

    /** The <code>OutputStream</code> to write on. */
    protected OutputStream out;

    private ThreadPoolTaskExecutor threadPoolTaskExecutor = loadThreadPool();

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public ExportToZipResourceWrapper retrieve(@PathVariable UUID uuid, HttpServletResponse response,
                                               HttpServletRequest request) throws SQLException {

        Context context = ContextUtil.obtainContext(request);
        Collection collection = ContentServiceFactory.getInstance().getCollectionService()
                                                     .find(context, uuid);
        List<ExportToZip> list = exportToZipService.findAllByStatusAndCollection(context, collection, "completed");
        List<ExportToZipRest> exportToZipRests = new LinkedList<>();
        for (ExportToZip exportToZip : list) {
            ExportToZipRest exportToZipRest = exportToZipConverter.fromModel(exportToZip);
            exportToZipRests.add(exportToZipRest);
        }

        ExportToZipRestWrapper exportToZipRestWrapper = new ExportToZipRestWrapper();
        exportToZipRestWrapper.setExportToZipRestList(exportToZipRests);
        ExportToZipResourceWrapper exportToZipResourceWrapper = new ExportToZipResourceWrapper(exportToZipRestWrapper,
                                                                                               utils);

        halLinkService.addLinks(exportToZipResourceWrapper);
        return exportToZipResourceWrapper;
    }

    private ThreadPoolTaskExecutor loadThreadPool() {
        DSpace dspace = new DSpace();
        org.dspace.kernel.ServiceManager manager = dspace.getServiceManager();
        return manager.getServiceByName("exportToZipThreadPool", ThreadPoolTaskExecutor.class);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/create")
    public ExportToZipResource create(@PathVariable UUID uuid, HttpServletResponse response,
                                      HttpServletRequest request)
        throws IOException, SQLException, AuthorizeException, ParseException {

        DCDate currentDate = DCDate.getCurrent();
        Context context = ContextUtil.obtainContext(request);
        Collection collection = collectionService.find(context, uuid);
        ExportToZip exportToZip = initializeExportToZip(collection, currentDate, context);
        threadPoolTaskExecutor.submit(new ExportToZipTask(context, collection, exportToZip.getID()));
        ExportToZipRest exportToZipRest = exportToZipConverter.fromModel(exportToZip);
        ExportToZipResource exportToZipResource = new ExportToZipResource(exportToZipRest, utils);
        halLinkService.addLinks(exportToZipResource);
        return exportToZipResource;
    }

    private ExportToZip initializeExportToZip(Collection collection, DCDate currentDate, Context context)
        throws SQLException, AuthorizeException, ParseException {
        ExportToZip exportToZip = new ExportToZip();
        exportToZip.setDso(collection);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = sf.format(currentDate.toDate());
        Date date = sf.parse(dateString);
        exportToZip.setDate(date);
        exportToZip.setStatus("In Progress");
        exportToZipService.create(context, exportToZip);
        exportToZipService.update(context, exportToZip);
        context.commit();
        return exportToZip;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/view/{dateString:.+}")
    public ExportToZipResource viewSpecific(@PathVariable UUID uuid,
                                            @PathVariable String dateString,
                                            HttpServletResponse response,
                                            HttpServletRequest request)
        throws IOException, SQLException, AuthorizeException, ParseException {

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sf.parse(dateString.replace("T", " "));
        if (date != null) {
            Context context = ContextUtil.obtainContext(request);
            Collection collection = ContentServiceFactory.getInstance().getCollectionService()
                                                         .find(context, uuid);
            ExportToZip exportToZip = exportToZipService.findByCollectionAndDate(context, collection, date);
            if (exportToZip != null) {
                ExportToZipResource exportToZipResource = new ExportToZipResource(
                    exportToZipConverter.fromModel(exportToZip), utils);
                halLinkService.addLinks(exportToZipResource);
                return exportToZipResource;
            }
        }
        return null;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/download/{dateString:.+}")
    public ExportToZipResource downloadSpecific(@PathVariable UUID uuid,
                                 @PathVariable String dateString,
                                 HttpServletResponse response,
                                 HttpServletRequest request)
        throws IOException, SQLException, AuthorizeException, ParseException {

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sf.parse(dateString.replace("T", " "));
        if (date != null) {
            Context context = ContextUtil.obtainContext(request);
            Collection collection = ContentServiceFactory.getInstance().getCollectionService()
                                                         .find(context, uuid);
            ExportToZip exportToZip = exportToZipService.findByCollectionAndDate(context, collection, date);
            if (exportToZip != null && StringUtils.equals(exportToZip.getStatus(), "completed")) {
                Bitstream bitstream = bitstreamService.find(context, exportToZip.getBitstreamId());

                BitstreamFormat format = bitstream.getFormat(context);
                InputStream inputstream = bitstreamService.retrieve(context, bitstream);
                MultipartFileSender sender = MultipartFileSender
                    .fromInputStream(inputstream)
                    .withBufferSize(BUFFER_SIZE)
                    .withFileName(getBitstreamName(bitstream, format))
                    .withLength(bitstream.getSize())
                    .withChecksum(bitstream.getChecksum())
                    .withMimetype(format.getMIMEType())
                    .withLastModified(bitstreamService.getLastModified(bitstream))
                    .withDisposition("attachment")
                    .with(request)
                    .with(response);

                if (sender.isNoRangeRequest() && isNotAnErrorResponse(response)) {
                    eventService.fireEvent(
                        new UsageEvent(
                            UsageEvent.Action.VIEW,
                            request,
                            context,
                            bitstream));
                }


                if (sender.isValid()) {
                    sender.serveResource();
                }

                context.complete();
            }
        }
        return null;
    }

    private String getBitstreamName(Bitstream bit, BitstreamFormat format) {
        String name = bit.getName();
        if (name == null) {
            // give a default name to the file based on the UUID and the primary extension of the format
            name = bit.getID().toString();
        }
        return name + ".zip";
    }

    private boolean isNotAnErrorResponse(HttpServletResponse response) {
        Response.Status.Family responseCode = Response.Status.Family.familyOf(response.getStatus());
        return responseCode.equals(Response.Status.Family.SUCCESSFUL)
            || responseCode.equals(Response.Status.Family.REDIRECTION);
    }
}

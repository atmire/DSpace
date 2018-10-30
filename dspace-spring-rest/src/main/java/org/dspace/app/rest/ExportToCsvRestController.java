package org.dspace.app.rest;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.dspace.app.rest.converter.ExportToCsvConverter;
import org.dspace.app.rest.link.HalLinkService;
import org.dspace.app.rest.model.ExportToCsvRest;
import org.dspace.app.rest.model.ExportToCsvRestWrapper;
import org.dspace.app.rest.model.hateoas.ExportToCsvResource;
import org.dspace.app.rest.model.hateoas.ExportToCsvResourceWrapper;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.MultipartFileSender;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToCsv;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.content.service.ExportToCsvService;
import org.dspace.core.Context;
import org.dspace.export.ExportStatus;
import org.dspace.export.ExportToCsvTask;
import org.dspace.services.EventService;
import org.dspace.usage.UsageEvent;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/{apiCategory}/{model}/" +
    "{uuid:[0-9a-fxA-FX]{8}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{12}}/exportToCsv")
public class ExportToCsvRestController {

    //Most file systems are configured to use block sizes of 4096 or 8192 and our buffer should be a multiple of that.
    private static final int BUFFER_SIZE = 4096 * 10;

    @Autowired
    private ExportToCsvConverter exportToCsvConverter;

    @Autowired
    protected Utils utils;

    @Autowired
    private ExportToCsvService exportToCsvService;

    @Autowired
    private HalLinkService halLinkService;

    @Autowired
    private BitstreamService bitstreamService;

    @Autowired
    private EventService eventService;

    @Autowired(required = true)
    private List<DSpaceObjectService<? extends DSpaceObject>> dSpaceObjectServices;

    @Autowired
    @Qualifier("exportThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, value = "/create")
    @PreAuthorize("hasPermission(#id, 'EPERSON', 'ADMIN')")
    public ExportToCsvResource create(@PathVariable UUID uuid, HttpServletResponse response,
                                      HttpServletRequest request, @PathVariable String model,
                                      @PathVariable String apiCategory)
        throws IOException, SQLException, AuthorizeException, ParseException {

        Context context = ContextUtil.obtainContext(request);
        DSpaceObject dSpaceObject = null;

        for (DSpaceObjectService dSpaceObjectService : dSpaceObjectServices) {
            dSpaceObject = dSpaceObjectService.find(context, uuid);
            if (dSpaceObject != null) {
                break;
            }
        }
        ExportToCsv exportToCsv = exportToCsvService.create(context, dSpaceObject);
        exportToCsvService.update(context, exportToCsv);
        context.commit();
        threadPoolTaskExecutor
            .submit(new ExportToCsvTask(dSpaceObject.getID(), exportToCsv.getDate()));
        ExportToCsvRest exportToCsvRest = exportToCsvConverter.fromModel(exportToCsv, model, apiCategory);
        ExportToCsvResource exportToCsvResource = new ExportToCsvResource(exportToCsvRest, utils);
        halLinkService.addLinks(exportToCsvResource);
        return exportToCsvResource;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ExportToCsvResourceWrapper retrieve(@PathVariable UUID uuid, HttpServletResponse response,
                                               HttpServletRequest request, @PathVariable String model,
                                               @PathVariable String apiCategory) throws SQLException {

        Context context = ContextUtil.obtainContext(request);


        DSpaceObject dSpaceObject = null;

        for (DSpaceObjectService dSpaceObjectService : dSpaceObjectServices) {
            dSpaceObject = dSpaceObjectService.find(context, uuid);
            if (dSpaceObject != null) {
                break;
            }
        }

        if (dSpaceObject == null) {
            throw new ResourceNotFoundException(apiCategory + "." + model + " with id: " + uuid + " not found");
        }

        List<ExportToCsv> list = exportToCsvService.findAllByDso(context, dSpaceObject);
        List<ExportToCsvRest> exportToCsvRests = new LinkedList<>();
        for (ExportToCsv exportToCsv : list) {
            ExportToCsvRest exportToCsvRest = exportToCsvConverter.fromModel(exportToCsv, model, apiCategory);
            exportToCsvRests.add(exportToCsvRest);
        }

        ExportToCsvRestWrapper exportToCsvRestWrapper = new ExportToCsvRestWrapper();
        exportToCsvRestWrapper.setExportToCsvRestList(exportToCsvRests);
        exportToCsvRestWrapper.setItemToBeExported(dSpaceObject);

        ExportToCsvResourceWrapper exportToCsvResourceWrapper = new ExportToCsvResourceWrapper(exportToCsvRestWrapper,
                                                                                               utils);

        halLinkService.addLinks(exportToCsvResourceWrapper);
        return exportToCsvResourceWrapper;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/view/{dateString:.+}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ExportToCsvResource viewSpecific(@PathVariable UUID uuid,
                                            @PathVariable String dateString,
                                            HttpServletResponse response,
                                            HttpServletRequest request, @PathVariable String model,
                                            @PathVariable String apiCategory)
        throws IOException, SQLException, AuthorizeException, ParseException {

        Date date = new DateTime(dateString).toDate();
        if (date != null) {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();

            DSpaceObject dSpaceObject = null;

            for (DSpaceObjectService dSpaceObjectService : dSpaceObjectServices) {
                dSpaceObject = dSpaceObjectService.find(context, uuid);
                if (dSpaceObject != null) {
                    break;
                }
            }
            ExportToCsv exportToCsv = exportToCsvService.findByDsoAndDate(context, dSpaceObject, date);
            if (exportToCsv != null) {
                ExportToCsvResource exportToCsvResource = new ExportToCsvResource(
                    exportToCsvConverter.fromModel(exportToCsv, model, apiCategory), utils);
                halLinkService.addLinks(exportToCsvResource);
                return exportToCsvResource;
            }
        }
        return null;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/download/{dateString:.+}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ExportToCsvResource downloadSpecific(@PathVariable UUID uuid,
                                                @PathVariable String dateString,
                                                HttpServletResponse response,
                                                HttpServletRequest request, @PathVariable String model,
                                                @PathVariable String apiCategory)
        throws IOException, SQLException, AuthorizeException, ParseException {

        Date date = new DateTime(dateString).toDate();
        if (date != null) {
            Context context = ContextUtil.obtainContext(request);
            DSpaceObject dSpaceObject = null;

            for (DSpaceObjectService dSpaceObjectService : dSpaceObjectServices) {
                dSpaceObject = dSpaceObjectService.find(context, uuid);
                if (dSpaceObject != null) {
                    break;
                }
            }
            ExportToCsv exportToCsv = exportToCsvService.findByDsoAndDate(context, dSpaceObject, date);
            if (exportToCsv != null && exportToCsv.getStatus().equals(ExportStatus.COMPLETED)) {
                Bitstream bitstream = bitstreamService.find(context, exportToCsv.getBitstreamId());

                BitstreamFormat format = bitstream.getFormat(context);
                InputStream inputstream = bitstreamService.retrieve(context, bitstream);
                MultipartFileSender sender = MultipartFileSender
                    .fromInputStream(inputstream)
                    .withBufferSize(BUFFER_SIZE)
                    .withFileName(getBitstreamName(bitstream, format))
                    .withLength(bitstream.getSizeBytes())
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
        return name + ".csv";
    }

    private boolean isNotAnErrorResponse(HttpServletResponse response) {
        Response.Status.Family responseCode = Response.Status.Family.familyOf(response.getStatus());
        return responseCode.equals(Response.Status.Family.SUCCESSFUL)
            || responseCode.equals(Response.Status.Family.REDIRECTION);
    }
}

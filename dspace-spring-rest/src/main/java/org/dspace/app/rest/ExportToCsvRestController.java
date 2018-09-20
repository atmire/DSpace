package org.dspace.app.rest;

import java.io.IOException;
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

import org.dspace.app.rest.converter.ExportToCsvConverter;
import org.dspace.app.rest.link.HalLinkService;
import org.dspace.app.rest.model.ExportToCsvRest;
import org.dspace.app.rest.model.ExportToCsvRestWrapper;
import org.dspace.app.rest.model.hateoas.DSpaceResource;
import org.dspace.app.rest.model.hateoas.ExportToCsvResource;
import org.dspace.app.rest.model.hateoas.ExportToCsvResourceWrapper;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DCDate;
import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToCsv;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.content.service.ExportToCsvService;
import org.dspace.core.Context;
import org.dspace.export.ExportToCsvTask;
import org.dspace.utils.DSpace;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ExportToCsvConverter exportToCsvConverter;

    @Autowired
    protected Utils utils;

    @Autowired
    private ExportToCsvService exportToCsvService;

    @Autowired
    private HalLinkService halLinkService;

    @Autowired(required = true)
    private List<DSpaceObjectService<? extends DSpaceObject>> dSpaceObjectServices;

    private ThreadPoolTaskExecutor threadPoolTaskExecutor = loadThreadPool();

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ExportToCsvResource create(@PathVariable UUID uuid, HttpServletResponse response,
                                 HttpServletRequest request, @PathVariable String model,
                                 @PathVariable String apiCategory)
        throws IOException, SQLException, AuthorizeException, ParseException {

        DCDate currentDate = DCDate.getCurrent();
        Context context = ContextUtil.obtainContext(request);
        DSpaceObject dSpaceObject = null;

        for (DSpaceObjectService dSpaceObjectService : dSpaceObjectServices) {
            dSpaceObject = dSpaceObjectService.find(context, uuid);
            if (dSpaceObject != null) {
                break;
            }
        }
        ExportToCsv exportToCsv = initializeExportToCsv(dSpaceObject, currentDate, context);
        threadPoolTaskExecutor.submit(new ExportToCsvTask(context.getCurrentUser(), dSpaceObject, exportToCsv.getID()));
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

    private ExportToCsv initializeExportToCsv(DSpaceObject dSpaceObject, DCDate currentDate, Context context)
        throws SQLException, AuthorizeException, ParseException {
        ExportToCsv exportToCsv = new ExportToCsv();
        exportToCsv.setDso(dSpaceObject);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = sf.format(currentDate.toDate());
        Date date = sf.parse(dateString);
        exportToCsv.setDate(date);
        exportToCsv.setStatus("In Progress");
        exportToCsvService.create(context, exportToCsv);
        exportToCsvService.update(context, exportToCsv);
        context.commit();
        return exportToCsv;
    }
    private ThreadPoolTaskExecutor loadThreadPool() {
        DSpace dspace = new DSpace();
        org.dspace.kernel.ServiceManager manager = dspace.getServiceManager();
        //TODO Look this up
        return manager.getServiceByName("exportToCsvThreadPool", ThreadPoolTaskExecutor.class);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "/view/{dateString:.+}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ExportToCsvResource viewSpecific(@PathVariable UUID uuid,
                                            @PathVariable String dateString,
                                            HttpServletResponse response,
                                            HttpServletRequest request, @PathVariable String model,
                                            @PathVariable String apiCategory)
        throws IOException, SQLException, AuthorizeException, ParseException {

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sf.parse(dateString.replace("T", " "));
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
}

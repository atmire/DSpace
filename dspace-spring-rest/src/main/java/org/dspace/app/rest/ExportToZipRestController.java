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

import org.dspace.app.rest.converter.ExportToZipConverter;
import org.dspace.app.rest.link.HalLinkService;
import org.dspace.app.rest.model.CollectionRest;
import org.dspace.app.rest.model.ExportToZipRest;
import org.dspace.app.rest.model.ExportToZipRestWrapper;
import org.dspace.app.rest.model.hateoas.ExportToZipResource;
import org.dspace.app.rest.model.hateoas.ExportToZipResourceWrapper;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.ExportToZip;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ExportToZipService;
import org.dspace.core.Context;
import org.dspace.export.ExportToZipTask;
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


    @Autowired
    ExportToZipConverter exportToZipConverter;

    @Autowired
    ExportToZipService exportToZipService;

    @Autowired
    protected Utils utils;

    @Autowired
    private HalLinkService halLinkService;

    private ThreadPoolTaskExecutor threadPoolTaskExecutor = loadThreadPool();

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public ExportToZipResourceWrapper retrieve(@PathVariable UUID uuid, HttpServletResponse response,
                                               HttpServletRequest request) throws SQLException {

        List<ExportToZip> list = exportToZipService.findAllByStatus(new Context(), "completed");
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
                                      HttpServletRequest request) throws IOException, SQLException, AuthorizeException {

        DCDate currentDate = DCDate.getCurrent();
        Context context = ContextUtil.obtainContext(request);
        Collection collection = ContentServiceFactory.getInstance().getCollectionService().find(context, uuid);
        ExportToZip exportToZip = initializeExportToZip(collection, currentDate, context);
        threadPoolTaskExecutor.submit(new ExportToZipTask(context, collection, exportToZip.getID()));
        ExportToZipRest exportToZipRest = exportToZipConverter.fromModel(exportToZip);
        ExportToZipResource exportToZipResource = new ExportToZipResource(exportToZipRest, utils);
        halLinkService.addLinks(exportToZipResource);
        return exportToZipResource;
    }

    private ExportToZip initializeExportToZip(Collection collection, DCDate currentDate, Context context)
        throws SQLException, AuthorizeException {
        ExportToZip exportToZip = new ExportToZip();
        exportToZip.setDso(collection);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        exportToZip.setDate(currentDate.toDate());
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

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = sf.parse(dateString.replace("T", " "));
        if (date != null) {
            Context context = new Context();
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
}

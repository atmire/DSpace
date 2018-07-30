package org.dspace.app.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dspace.app.rest.model.CollectionRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DCDate;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ExportToZipService;
import org.dspace.export.ExportToZipTask;
import org.dspace.utils.DSpace;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/" + CollectionRest.CATEGORY + "/" + CollectionRest.PLURAL_NAME
    + "/{uuid:[0-9a-fxA-FX]{8}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{12}}/exportToZip")
public class ExportToZipRestController {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor = loadThreadPool();
    ExportToZipService exportToZipService = ContentServiceFactory.getInstance().getExportToZipService();

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public void retrieve(@PathVariable UUID uuid, HttpServletResponse response,
                         HttpServletRequest request) throws IOException, SQLException, AuthorizeException {

        DCDate currentDate = DCDate.getCurrent();
        threadPoolTaskExecutor.submit(new ExportToZipTask(uuid, currentDate));
    }

    private ThreadPoolTaskExecutor loadThreadPool() {
        DSpace dspace = new DSpace();
        org.dspace.kernel.ServiceManager manager = dspace.getServiceManager();
        return manager.getServiceByName("exportToZipThreadPool", ThreadPoolTaskExecutor.class);
    }
}

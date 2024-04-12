package org.dspace.app.rest;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dspace.app.rest.utils.HttpHeadersInitializer;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/")
@RestController
public class AtmireVersionsController {

    protected ConfigurationService configurationService
        = DSpaceServicesFactory.getInstance().getConfigurationService();

    public static final String ATMIRE_VERSIONS_FILE  = "atmire-versions.json";
    public static final int BUFFER_SIZE = 4096 * 10;
    private static final String mimetype = "applicaton/json";

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(value = "/atmire-versions", method = RequestMethod.GET)
    public ResponseEntity<String> atmireVersioning(HttpServletRequest request, HttpServletResponse response) {

        // get the directory location of the atmire-versioning file out of the config
        String directory = configurationService.getProperty("atmire-versions.directory");
        if (!directory.endsWith("/")) {
            directory += '/';
        }
        final String file = directory + ATMIRE_VERSIONS_FILE;

        try {
            // read the file
            String json = readFileAsJson(file);

            HttpHeadersInitializer httpHeadersInitializer = new HttpHeadersInitializer()
                .withBufferSize(BUFFER_SIZE)
                .withFileName(ATMIRE_VERSIONS_FILE)
                .withMimetype(mimetype)
                .with(request)
                .with(response);
            if (httpHeadersInitializer.isValid()) {
                HttpHeaders httpHeaders = httpHeadersInitializer.initialiseHeaders();
                return ResponseEntity.ok().headers(httpHeaders).body(json);

            }
            return ResponseEntity.ok().body(json);

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Could not find " + file);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Something went wrong reading " + file);
        }
    }

    /**
     * This method reads a json file and returns it's content
     * @param filePath file to read
     * @return content of the file
     * @throws IOException file not found, something went wrong trying to read the file
     */
    public String readFileAsJson(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);
        return objectMapper.writeValueAsString(objectMapper.readTree(file));
    }


}

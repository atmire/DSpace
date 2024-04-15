package org.dspace.app.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
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

    public static final String ATMIRE_VERSIONS_CONFIG  = "atmire-versions.file";
    public static final int BUFFER_SIZE = 4096 * 10;
    private static final String mimetype = MediaType.APPLICATION_JSON;

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(value = "/atmire-versions", method = RequestMethod.GET)
    public ResponseEntity<String> atmireVersioning(HttpServletRequest request, HttpServletResponse response) {

        // get the file location
        String file = getFilePath();

        try {
            // read the file
            String json = readFileAsJson(file);

            // setup headers
            HttpHeadersInitializer httpHeadersInitializer = new HttpHeadersInitializer()
                .withBufferSize(BUFFER_SIZE)
                .withFileName(getFileNameFromPath(file))
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

    private String getFilePath() {
        String file = configurationService.getProperty(ATMIRE_VERSIONS_CONFIG);
        if (StringUtils.isBlank(file)) {
            throw new IllegalArgumentException(ATMIRE_VERSIONS_CONFIG + " was not correctly defined in the config.");
        }
        return file;
    }

    /**
     * This method will give the filename that is specified at the end of a path to a file
     * @param path the path to the file
     * @return the filename at the end of the path
     */
    private static String getFileNameFromPath(String path) {
        // get filename
        String[] parts = path.split("/");
        return parts[parts.length - 1];
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

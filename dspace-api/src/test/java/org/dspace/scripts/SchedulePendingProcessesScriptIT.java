/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.scripts;

import static org.dspace.api.token.service.ApiTokenService.API_TOKEN_HEADER;
import static org.dspace.api.token.service.ApiTokenService.API_USER_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.builder.ProcessBuilder;
import org.dspace.content.ProcessStatus;
import org.dspace.scripts.factory.ScriptServiceFactory;
import org.dspace.scripts.service.ProcessService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.util.HttpClientFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class SchedulePendingProcessesScriptIT extends AbstractIntegrationTestWithDatabase {

    private static final String API_TOKEN = "api-token";

    private final ProcessService processService = ScriptServiceFactory.getInstance().getProcessService();
    private final ConfigurationService configurationService =
            DSpaceServicesFactory.getInstance().getConfigurationService();

    private HttpClient mockHttpClient;
    private HttpResponse<String> mockResponse;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        context.turnOffAuthorisationSystem();
        Process process1 = ProcessBuilder.createProcess(context, admin, "false-start-mock-script", new ArrayList<>())
                                         .withProcessStatus(ProcessStatus.PENDING)
                                         .build();
        Process process2 = ProcessBuilder.createProcess(context, admin, "false-start-mock-script", new ArrayList<>())
                                         .withProcessStatus(ProcessStatus.PENDING)
                                         .build();
        Process process3 = ProcessBuilder.createProcess(context, admin, "false-start-mock-script", new ArrayList<>())
                                         .withProcessStatus(ProcessStatus.PENDING)
                                         .build();
        context.restoreAuthSystemState();
        context.commit();

        configurationService.setProperty("api.token", API_TOKEN);

        mockHttpClient = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.headers()).thenReturn(HttpHeaders.of(
                Map.of("Set-Cookie", List.of("DSPACE-XSRF-COOKIE=CSRF-TOKEN;SUFFIX")), (name, value) -> true));
        when(mockHttpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
    }

    @Test
    public void testCreatedProcessesHavePendingStatus() throws Exception {
        List<Process> processes = processService.findAll(context);

        assertEquals(3, processes.size());
        processes.forEach(process ->
            assertEquals(ProcessStatus.PENDING, process.getProcessStatus()));
    }

    @Test
    public void testProcessesShouldNotStartWithoutEmailParameter() throws Exception {
        String[] args = new String[] { "schedule-pending-processes" };
        runDSpaceScript(args);

        List<Process> processes = processService.findAll(context);
        assertEquals(3, processes.size());
        processes.forEach(process -> assertEquals(ProcessStatus.PENDING, process.getProcessStatus()));
    }

    @Test
    public void testProcessesShouldNotStartWithEPersonEmailParameter() throws Exception {
        String[] args = new String[] { "schedule-pending-processes", "-e", eperson.getEmail() };
        runDSpaceScript(args);

        List<Process> processes = processService.findAll(context);
        assertEquals(3, processes.size());
        processes.forEach(process -> assertEquals(ProcessStatus.PENDING, process.getProcessStatus()));
    }

    @Test
    public void testScriptShouldSendHttpRequestWithCorrectHeaders() throws Exception {
        String[] args = new String[] { "schedule-pending-processes", "-e", admin.getEmail() };

        try (MockedStatic<HttpClientFactory> mocked = mockStatic(HttpClientFactory.class)) {
            mocked.when(HttpClientFactory::getHttpClient).thenReturn(mockHttpClient);
            runDSpaceScript(args);
        }

        ArgumentCaptor<HttpRequest> requestCaptor =
                ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient, times(4)).send(requestCaptor.capture(), any());

        // Only get the request headers that were sent with the second call
        HttpHeaders headers = requestCaptor.getAllValues().get(1).headers();

        assertTrue(headers.firstValue(API_USER_HEADER).isPresent());
        assertTrue(headers.firstValue(API_TOKEN_HEADER).isPresent());

        assertEquals(admin.getID().toString(), headers.firstValue(API_USER_HEADER).get());
        assertEquals(API_TOKEN, headers.firstValue(API_TOKEN_HEADER).get());
    }

    @Test
    public void testScriptShouldSendHttpRequestForEveryPendingProcess() throws Exception {
        context.turnOffAuthorisationSystem();
        Process process4 = ProcessBuilder.createProcess(context, admin, "false-start-mock-script", new ArrayList<>())
                                         .withProcessStatus(ProcessStatus.COMPLETED)
                                         .build();
        context.restoreAuthSystemState();
        context.commit();

        String[] args = new String[] { "schedule-pending-processes", "-e", admin.getEmail() };

        try (MockedStatic<HttpClientFactory> mocked = mockStatic(HttpClientFactory.class)) {
            mocked.when(HttpClientFactory::getHttpClient).thenReturn(mockHttpClient);
            runDSpaceScript(args);
        }

        // Verify HttpClient.send() gets called 4 times (once for the CSRF token and 3 times for the pending processes)
        verify(mockHttpClient, times(4))
                .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void testScriptShouldNotSendHttpRequestWhenNoCsrfTokenWasFound() throws Exception {
        String[] args = new String[] { "schedule-pending-processes", "-e", admin.getEmail() };

        when(mockResponse.headers()).thenReturn(HttpHeaders.of(
                Map.of("Set-Cookie", List.of("NO-CSRF-TOKEN")), (name, value) -> true));
        when(mockHttpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        try (MockedStatic<HttpClientFactory> mocked = mockStatic(HttpClientFactory.class)) {
            mocked.when(HttpClientFactory::getHttpClient).thenReturn(mockHttpClient);
            runDSpaceScript(args);
        }

        // Verify HttpClient.send() gets called once and stops when it fails to retrieve CSRF token
        verify(mockHttpClient, times(1))
                .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}

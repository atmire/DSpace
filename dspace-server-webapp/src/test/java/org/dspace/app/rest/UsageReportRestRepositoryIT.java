/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.dspace.app.rest.builder.BitstreamBuilder.createBitstream;
import static org.dspace.app.rest.builder.CollectionBuilder.createCollection;
import static org.dspace.app.rest.builder.CommunityBuilder.createCommunity;
import static org.dspace.app.rest.builder.CommunityBuilder.createSubCommunity;
import static org.dspace.app.rest.builder.ItemBuilder.createItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dspace.app.rest.matcher.UsageReportMatcher;
import org.dspace.app.rest.model.UsageReportPointDateRest;
import org.dspace.app.rest.model.UsageReportPointDsoTotalVisitsRest;
import org.dspace.app.rest.model.UsageReportPointRest;
import org.dspace.app.rest.model.ViewEventRest;
import org.dspace.app.rest.repository.UsageReportRestRepository;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.statistics.factory.StatisticsServiceFactory;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Integration test to test the /api/statistics/usagereports/ endpoints of {@link UsageReportRestRepository}
 *
 * @author Maria Verdonck (Atmire) on 10/06/2020
 */
public class UsageReportRestRepositoryIT extends AbstractControllerIntegrationTest {

    private Community communityNotVisited;
    private Community communityVisited;
    private Collection collectionNotVisited;
    private Collection collectionVisited;
    private Item itemNotVisited;
    private Item itemVisited;
    private Bitstream bitstreamNotVisited;
    private Bitstream bitstreamVisited;
    private String loggedInToken;

    private static final String TOTAL_VISITS_REPORT_ID = "TotalVisits";
    private static final String TOTAL_VISITS_PER_MONTH_REPORT_ID = "TotalVisitsPerMonth";

    @BeforeClass
    public static void clearStatistics() throws Exception {
        // To ensure these tests start "fresh", clear out any existing statistics data.
        // NOTE: this is committed immediately in removeIndex()
        StatisticsServiceFactory.getInstance().getSolrLoggerService().removeIndex("*:*");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        context.turnOffAuthorisationSystem();

        Community community = createCommunity(context).build();
        communityNotVisited = createSubCommunity(context, community).build();
        communityVisited = createSubCommunity(context, community).build();
        collectionNotVisited = createCollection(context, community).build();
        collectionVisited = createCollection(context, community).build();
        itemVisited = createItem(context, collectionNotVisited).build();
        itemNotVisited = createItem(context, collectionNotVisited).build();
        bitstreamNotVisited = createBitstream(context, itemNotVisited, toInputStream("test", UTF_8)).build();
        bitstreamVisited = createBitstream(context, itemNotVisited, toInputStream("test", UTF_8)).build();

        loggedInToken = getAuthToken(eperson.getEmail(), password);

        context.restoreAuthSystemState();
    }

    @Test
    public void usagereports_withoutId_NotImplementedException() throws Exception {
        getClient().perform(get("/api/statistics/usagereports"))
                   .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()));
    }

    @Test
    public void usagereports_notProperUUIDAndReportId_Exception() throws Exception {
        getClient().perform(get("/api/statistics/usagereports/notProperUUIDAndReportId"))
                   .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void usagereports_nonValidUUIDpart_Exception() throws Exception {
        getClient().perform(get("/api/statistics/usagereports/notAnUUID_TotalVisits"))
                   .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void usagereports_nonValidReportIDpart_Exception() throws Exception {
        getClient().perform(get("/api/statistics/usagereports/" + UUID.randomUUID() + "_NotValidReport"))
                   .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void usagereports_NonExistentUUID_Exception() throws Exception {
        getClient().perform(get("/api/statistics/usagereports/" + UUID.randomUUID() + "_TotalVisits"))
                   .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void totalVisitsReport_Community_Visited() throws Exception {
        //** WHEN **
        //We visit the community
        ViewEventRest viewEventRest = new ViewEventRest();
        viewEventRest.setTargetType("community");
        viewEventRest.setTargetId(communityVisited.getID());

        ObjectMapper mapper = new ObjectMapper();

        getClient(loggedInToken).perform(post("/api/statistics/viewevents")
            .content(mapper.writeValueAsBytes(viewEventRest))
            .contentType(contentType))
                                .andExpect(status().isCreated());

        UsageReportPointDsoTotalVisitsRest expectedPoint = new UsageReportPointDsoTotalVisitsRest();
        expectedPoint.addValue("views", 1);
        expectedPoint.setType("community");
        expectedPoint.setId(communityVisited.getID().toString());

        // And request that community's TotalVisits stat report
        getClient().perform(
            get("/api/statistics/usagereports/" + communityVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID))
                   //** THEN **
                   .andExpect(status().isOk())

                   .andExpect(jsonPath("$", Matchers.is(
                       UsageReportMatcher
                           .matchUsageReport(communityVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID,
                               TOTAL_VISITS_REPORT_ID, Arrays.asList(expectedPoint))))
                             );
    }

    @Test
    public void totalVisitsReport_Community_NotVisited() throws Exception {
        //** WHEN **
        //Community is never visited
        UsageReportPointDsoTotalVisitsRest expectedPoint = new UsageReportPointDsoTotalVisitsRest();
        expectedPoint.addValue("views", 0);
        expectedPoint.setType("community");
        expectedPoint.setId(communityNotVisited.getID().toString());

        // And request that community's TotalVisits stat report
        getClient().perform(
            get("/api/statistics/usagereports/" + communityNotVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID))
                   //** THEN **
                   .andExpect(status().isOk())

                   .andExpect(jsonPath("$", Matchers.is(
                       UsageReportMatcher
                           .matchUsageReport(communityNotVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID,
                               TOTAL_VISITS_REPORT_ID, Arrays.asList(expectedPoint))))
                             );
    }

    @Test
    public void totalVisitsReport_Collection_Visited() throws Exception {
        //** WHEN **
        //We visit the collection twice
        ViewEventRest viewEventRest = new ViewEventRest();
        viewEventRest.setTargetType("collection");
        viewEventRest.setTargetId(collectionVisited.getID());

        ObjectMapper mapper = new ObjectMapper();

        getClient(loggedInToken).perform(post("/api/statistics/viewevents")
            .content(mapper.writeValueAsBytes(viewEventRest))
            .contentType(contentType))
                                .andExpect(status().isCreated());

        getClient(loggedInToken).perform(post("/api/statistics/viewevents")
            .content(mapper.writeValueAsBytes(viewEventRest))
            .contentType(contentType))
                                .andExpect(status().isCreated());

        UsageReportPointDsoTotalVisitsRest expectedPoint = new UsageReportPointDsoTotalVisitsRest();
        expectedPoint.addValue("views", 2);
        expectedPoint.setType("collection");
        expectedPoint.setId(collectionVisited.getID().toString());

        // And request that collection's TotalVisits stat report
        getClient().perform(
            get("/api/statistics/usagereports/" + collectionVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID))
                   //** THEN **
                   .andExpect(status().isOk())

                   .andExpect(jsonPath("$", Matchers.is(
                       UsageReportMatcher
                           .matchUsageReport(collectionVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID,
                               TOTAL_VISITS_REPORT_ID, Arrays.asList(expectedPoint))))
                             );
    }

    @Test
    public void totalVisitsReport_Collection_NotVisited() throws Exception {
        //** WHEN **
        //Collection is never visited
        UsageReportPointDsoTotalVisitsRest expectedPoint = new UsageReportPointDsoTotalVisitsRest();
        expectedPoint.addValue("views", 0);
        expectedPoint.setType("collection");
        expectedPoint.setId(collectionNotVisited.getID().toString());

        // And request that collection's TotalVisits stat report
        getClient().perform(
            get("/api/statistics/usagereports/" + collectionNotVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID))
                   //** THEN **
                   .andExpect(status().isOk())

                   .andExpect(jsonPath("$", Matchers.is(
                       UsageReportMatcher
                           .matchUsageReport(collectionNotVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID,
                               TOTAL_VISITS_REPORT_ID, Arrays.asList(expectedPoint))))
                             );
    }

    @Test
    public void totalVisitsReport_Item_Visited() throws Exception {
        //** WHEN **
        //We visit an Item
        ViewEventRest viewEventRest = new ViewEventRest();
        viewEventRest.setTargetType("item");
        viewEventRest.setTargetId(itemVisited.getID());

        ObjectMapper mapper = new ObjectMapper();

        getClient(loggedInToken).perform(post("/api/statistics/viewevents")
            .content(mapper.writeValueAsBytes(viewEventRest))
            .contentType(contentType))
                                .andExpect(status().isCreated());

        UsageReportPointDsoTotalVisitsRest expectedPoint = new UsageReportPointDsoTotalVisitsRest();
        expectedPoint.addValue("views", 1);
        expectedPoint.setType("item");
        expectedPoint.setId(itemVisited.getID().toString());

        // And request that collection's TotalVisits stat report
        getClient().perform(
            get("/api/statistics/usagereports/" + itemVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID))
                   //** THEN **
                   .andExpect(status().isOk())

                   .andExpect(jsonPath("$", Matchers.is(
                       UsageReportMatcher
                           .matchUsageReport(itemVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID,
                               TOTAL_VISITS_REPORT_ID, Arrays.asList(expectedPoint))))
                             );
    }

    @Test
    public void totalVisitsReport_Item_NotVisited() throws Exception {
        //** WHEN **
        //Item is never visited
        UsageReportPointDsoTotalVisitsRest expectedPoint = new UsageReportPointDsoTotalVisitsRest();
        expectedPoint.addValue("views", 0);
        expectedPoint.setType("item");
        expectedPoint.setId(itemNotVisited.getID().toString());

        // And request that item's TotalVisits stat report
        getClient().perform(
            get("/api/statistics/usagereports/" + itemNotVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID))
                   //** THEN **
                   .andExpect(status().isOk())

                   .andExpect(jsonPath("$", Matchers.is(
                       UsageReportMatcher
                           .matchUsageReport(itemNotVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID,
                               TOTAL_VISITS_REPORT_ID, Arrays.asList(expectedPoint))))
                             );
    }

    @Test
    public void totalVisitsReport_Bitstream_Visited() throws Exception {
        //** WHEN **
        //We visit a Bitstream
        ViewEventRest viewEventRest = new ViewEventRest();
        viewEventRest.setTargetType("bitstream");
        viewEventRest.setTargetId(bitstreamVisited.getID());

        ObjectMapper mapper = new ObjectMapper();

        getClient(loggedInToken).perform(post("/api/statistics/viewevents")
            .content(mapper.writeValueAsBytes(viewEventRest))
            .contentType(contentType))
                                .andExpect(status().isCreated());

        UsageReportPointDsoTotalVisitsRest expectedPoint = new UsageReportPointDsoTotalVisitsRest();
        expectedPoint.addValue("views", 1);
        expectedPoint.setType("bitstream");
        expectedPoint.setId(bitstreamVisited.getID().toString());

        // And request that bitstream's TotalVisits stat report
        getClient().perform(
            get("/api/statistics/usagereports/" + bitstreamVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID))
                   //** THEN **
                   .andExpect(status().isOk())

                   .andExpect(jsonPath("$", Matchers.is(
                       UsageReportMatcher
                           .matchUsageReport(bitstreamVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID,
                               TOTAL_VISITS_REPORT_ID, Arrays.asList(expectedPoint))))
                             );
    }

    @Test
    public void totalVisitsReport_Bitstream_NotVisited() throws Exception {
        //** WHEN **
        //Bitstream is never visited
        UsageReportPointDsoTotalVisitsRest expectedPoint = new UsageReportPointDsoTotalVisitsRest();
        expectedPoint.addValue("views", 0);
        expectedPoint.setType("bitstream");
        expectedPoint.setId(bitstreamNotVisited.getID().toString());

        // And request that bitstream's TotalVisits stat report
        getClient().perform(
            get("/api/statistics/usagereports/" + bitstreamNotVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID))
                   //** THEN **
                   .andExpect(status().isOk())

                   .andExpect(jsonPath("$", Matchers.is(
                       UsageReportMatcher
                           .matchUsageReport(bitstreamNotVisited.getID() + "_" + TOTAL_VISITS_REPORT_ID,
                               TOTAL_VISITS_REPORT_ID, Arrays.asList(expectedPoint))))
                             );
    }

    @Test
    public void totalVisitsPerMonthReport_Item_Visited() throws Exception {
        //** WHEN **
        //We visit an Item
        ViewEventRest viewEventRest = new ViewEventRest();
        viewEventRest.setTargetType("item");
        viewEventRest.setTargetId(itemVisited.getID());

        ObjectMapper mapper = new ObjectMapper();

        getClient(loggedInToken).perform(post("/api/statistics/viewevents")
            .content(mapper.writeValueAsBytes(viewEventRest))
            .contentType(contentType))
                                .andExpect(status().isCreated());

        // Create expected points from -6 months to now, with a view in current month
        List<UsageReportPointRest> expectedPoints = new ArrayList<>();
        int nrOfMonthsBack = 6;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i <= nrOfMonthsBack; i++) {
            UsageReportPointDateRest expectedPoint = new UsageReportPointDateRest();
            if (i > 0) {
                expectedPoint.addValue("views", 0);
            } else {
                expectedPoint.addValue("views", 1);
            }
            String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            expectedPoint.setId(month + " " + cal.get(Calendar.YEAR));

            expectedPoints.add(expectedPoint);
            cal.add(Calendar.MONTH, -1);
        }

        // And request that collection's TotalVisits stat report
        getClient().perform(
            get("/api/statistics/usagereports/" + itemVisited.getID() + "_" + TOTAL_VISITS_PER_MONTH_REPORT_ID))
                   //** THEN **
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$", Matchers.is(
                       UsageReportMatcher
                           .matchUsageReport(itemVisited.getID() + "_" + TOTAL_VISITS_PER_MONTH_REPORT_ID,
                               TOTAL_VISITS_PER_MONTH_REPORT_ID, expectedPoints))));
    }

    @Test
    public void totalVisitsPerMonthReport_Collection_Visited() throws Exception {
        //** WHEN **
        //We visit an Item
        ViewEventRest viewEventRest = new ViewEventRest();
        viewEventRest.setTargetType("collection");
        viewEventRest.setTargetId(collectionVisited.getID());

        ObjectMapper mapper = new ObjectMapper();

        getClient(loggedInToken).perform(post("/api/statistics/viewevents")
            .content(mapper.writeValueAsBytes(viewEventRest))
            .contentType(contentType))
                                .andExpect(status().isCreated());

        // Create expected points from -6 months to now, with a view in current month
        List<UsageReportPointRest> expectedPoints = new ArrayList<>();
        int nrOfMonthsBack = 6;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i <= nrOfMonthsBack; i++) {
            UsageReportPointDateRest expectedPoint = new UsageReportPointDateRest();
            if (i > 0) {
                expectedPoint.addValue("views", 0);
            } else {
                expectedPoint.addValue("views", 1);
            }
            String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            expectedPoint.setId(month + " " + cal.get(Calendar.YEAR));

            expectedPoints.add(expectedPoint);
            cal.add(Calendar.MONTH, -1);
        }

        // And request that collection's TotalVisits stat report
        getClient().perform(
            get("/api/statistics/usagereports/" + collectionVisited.getID() + "_" + TOTAL_VISITS_PER_MONTH_REPORT_ID))
                   //** THEN **
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$", Matchers.is(
                       UsageReportMatcher
                           .matchUsageReport(collectionVisited.getID() + "_" + TOTAL_VISITS_PER_MONTH_REPORT_ID,
                               TOTAL_VISITS_PER_MONTH_REPORT_ID, expectedPoints))));
    }
}

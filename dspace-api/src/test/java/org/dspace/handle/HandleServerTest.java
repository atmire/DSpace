/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle;

import net.cnri.util.StreamTable;
import net.handle.server.Main;
import org.dspace.AbstractIntegrationTestWithDatabase;
import org.junit.Test;

import java.io.File;

public class HandleServerTest
        extends AbstractIntegrationTestWithDatabase {

    @Test
    public void testStartHandleServer() throws Exception {
        StreamTable configTable = new StreamTable();
//        configTable.readFrom(getClass().getResourceAsStream("config.dct"));
        String config = "{\n" +
                "  \"hdl_http_config\" = {\n" +
                "    \"bind_address\" = \"127.0.0.1\"\n" +
                "    \"num_threads\" = \"15\"\n" +
                "    \"bind_port\" = \"8000\"\n" +
                "    \"log_accesses\" = \"yes\"\n" +
                "  }\n" +
                "\n" +
                "  \"server_type\" = \"server\"\n" +
                "\n" +
                "  \"log_save_config\" = {\n" +
                "    \"log_save_directory\" = \"logs\"\n" +
                "    \"log_save_interval\" = \"Monthly\"\n" +
                "  }\n" +
                "\n" +
                "  \"no_udp_resolution\" = \"yes\"\n" +
                "  \"interfaces\" = (\n" +
                "    \"hdl_http\"\n" +
                "  )\n" +
                "\n" +
                "  \"server_config\" = {\n" +
                "\"storage_type\" = \"CUSTOM\"\n" +
                "\"storage_class\" = \"org.dspace.handle.HandlePlugin\"\n" +
                "\"enable_txn_queue\" = \"no\"\n" +
                "\n" +
                "    \"server_admins\" = (\n" +
                "      \"300:0.NA/123456789\"\n" +
                "    )\n" +
                "\n" +
                "    \"replication_admins\" = (\n" +
                "      \"300:0.NA/123456789\"\n" +
                "    )\n" +
                "\n" +
                "    \"max_session_time\" = \"86400000\"\n" +
                "    \"this_server_id\" = \"1\"\n" +
                "    \"max_auth_time\" = \"60000\"\n" +
                "    \"server_admin_full_access\" = \"yes\"\n" +
                "    \"case_sensitive\" = \"no\"\n" +
                "    \"auto_homed_prefixes\" = (\n" +
                "      \"0.NA/123456789\"\n" +
                "    )\n" +
                "\n" +
                "  }\n" +
                "\n" +
                "}\n";
        configTable.readFrom(config);
        Main main = new Main(new File(testProps.getProperty("test.folder")), configTable);
        main.initialize();
        //just test there's no exception
    }
}

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
import net.handle.server.SimpleSetup;
import org.dspace.AbstractIntegrationTestWithDatabase;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;

public class HandleServerTest
        extends AbstractIntegrationTestWithDatabase {

    @Test
    public void testStartHandleServer() throws Exception {
        StreamTable configTable = new StreamTable();
        InputStream old = System.in;
        try {
            System.setIn(getClass().getResourceAsStream("setup.dct"));
            SimpleSetup.main(new String[]{testProps.getProperty("test.handle.dir")});
        } finally {
            System.setIn(old);
        }
        configTable.readFromFile(testProps.getProperty("test.handle.dir")+"/config.dct");
        Main main = new Main(new File(testProps.getProperty("test.handle.dir")), configTable);
        main.initialize();
        //just test there's no exception
        main.start();
    }
}

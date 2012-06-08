/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.dspace.statistics.SolrLogger;

/**
 * SpiderDetector is used to find IP's that are spiders...
 * In future someone may add Host Domains
 * to the detection criteria here.
 *
 * @author kevinvandevelde at atmire.com
 * @author ben at atmire.com
 * @author Mark Diggory (mdiggory at atmire.com)
 */
public class SpiderDetector {

    private static Logger log = Logger.getLogger(SpiderDetector.class);

    private static Boolean useProxies;

    /**
     * Sparse HashTable structure to hold IP address ranges.
     */
    private static IPTable table = null;

    /** Collection of regular expressions to match known spiders' agents */
    private static List<Pattern> agents = null;

    /**
     * Utility method which reads IP addresses from a file & returns them in a Set.
     *
     * @param spiderIpFile the location of our spider file
     * @return a vector full of IPs
     * @throws IOException could not happen since we check the file be4 we use it
     */
    public static Set<String> readIpAddresses(File spiderIpFile) throws IOException {
        Set<String> ips = new HashSet<String>();

        if (!spiderIpFile.exists() || !spiderIpFile.isFile())
        {
            return ips;
        }

        //Read our file & get all them ip's
        BufferedReader in = new BufferedReader(new FileReader(spiderIpFile));
        String line;
        while ((line = in.readLine()) != null) {
            if (!line.startsWith("#")) {
                line = line.trim();

                if (!line.equals("") && !Character.isDigit(line.charAt(0))) {
                    // is a hostname
                    // add this functionality later...
                } else if (!line.equals("")) {
                    ips.add(line);
                    // is full v4 ip (too tired to deal with v6)...
                }
            } else {
                //   ua.add(line.replaceFirst("#","").replaceFirst("UA","").trim());
                // ... add this functionality later
            }
        }
        in.close();
        return ips;
    }

    /**
     * Let e.g. Spring add agent patterns to our collection.
     * This can be called more than once, and will simply augment the collection
     * with new patterns.
     * 
     * @param agentPatterns
     * @throws PatternSyntaxExpression
     */
    static public void setAgentPatterns(List<String> agentPatterns)
    {
        if (null == agents)
            agents = new ArrayList<Pattern>(agentPatterns.size());

        int nPatterns = 0;
        for (String agentPattern : agentPatterns)
        {
            Pattern newPattern = Pattern.compile(agentPattern);
            agents.add(newPattern);
            nPatterns++;
        }
        log.info("Received " + String.valueOf(nPatterns) + " agent patterns.");
    }

    /**
     * Empty the agent pattern list.
     */
    static void clearAgentPatterns()
    {
        if (null == agents)
            agents = new ArrayList<Pattern>();
        else
            agents.clear();
    }

    /**
     * Get an immutable Set representing all the Spider Addresses here
     *
     * @return
     */
    public static Set<String> getSpiderIpAddresses() {

        loadSpiderIpAddresses();
        return table.toSet();
    }

    /*
        private loader to populate the table from files.
     */

    private static void loadSpiderIpAddresses() {


        if (table == null) {
            table = new IPTable();

            String filePath = ConfigurationManager.getProperty("dspace.dir");

            try {
                File spidersDir = new File(filePath, "config/spiders");

                if (spidersDir.exists() && spidersDir.isDirectory()) {
                    for (File file : spidersDir.listFiles()) {
                        for (String ip : readIpAddresses(file)) {
                            table.add(ip);
                        }
                        log.info("Loaded Spider IP file: " + file);
                    }
                } else {
                    log.info("No spider file loaded");
                }


            }
            catch (Exception e) {
                log.error("Error Loading Spiders:" + e.getMessage(), e);
            }


        }

    }


    /**
     * Static Service Method for testing spiders against existing spider files.
     * <p>
     * In the future this will be extended to support Domain Name detection.
     * <p>
     * In future spiders HashSet may be optimized as byte offset array to
     * improve performance and memory footprint further.
     *
     * @param request
     * @return true|false if the request was detected to be from a spider
     */
    public static boolean isSpider(HttpServletRequest request) {

        // See if any agent patterns match
        String agent = request.getHeader("User-Agent");
        if ((null != agent) && (null != agents))
            for (Pattern candidate : agents)
            {
                if (candidate.matcher(agent).find())
                    return true;
            }

        // No.  See if any IP addresses match
        if (isUseProxies() && request.getHeader("X-Forwarded-For") != null) {
            /* This header is a comma delimited list */
            for (String xfip : request.getHeader("X-Forwarded-For").split(",")) {
                if (isSpider(xfip))
                {
                    return true;
                }
            }
        }

        return isSpider(request.getRemoteAddr());

    }

    /**
     * Check individual IP is a spider.
     *
     * @param ip
     * @return if is spider IP
     */
    public static boolean isSpider(String ip) {

        if (table == null) {
            SpiderDetector.loadSpiderIpAddresses();
        }

        try {
            if (table.contains(ip)) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;


    }

    private static boolean isUseProxies() {
        if(useProxies == null) {
            if ("true".equals(ConfigurationManager.getProperty("useProxies")))
            {
                useProxies = true;
            }
            else
            {
                useProxies = false;
            }
        }

        return useProxies;
    }


}

/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics.util;

import java.net.InetAddress;

import com.google.common.net.InetAddresses;

/**
 * Helpers for parsing IP address literals without DNS lookups.
 */
public final class IpAddressUtils {

    private static final String IP_ADDR_MARKER = "ip_addr=";

    private IpAddressUtils() {
    }

    /**
     * Whether the value is a syntactically valid IPv4 or IPv6 address literal.
     *
     * @param value candidate IP string
     * @return {@code true} when the value is a valid IP literal
     */
    public static boolean isIpLiteral(String value) {
        if (value == null || value.isEmpty() || "unknown".equalsIgnoreCase(value)) {
            return false;
        }
        try {
            InetAddresses.forString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parse an IPv4 or IPv6 address literal.
     *
     * @param value IP string
     * @return parsed address
     * @throws IllegalArgumentException when the value is not a valid IP literal
     */
    public static InetAddress parseIpLiteral(String value) {
        return InetAddresses.forString(value);
    }

    /**
     * Extract the client IP from a {@code dspace.log} line's {@code ip_addr=} field.
     * Supports both IPv4 and IPv6; returns {@code unknown} when no valid literal is found.
     *
     * @param line log line
     * @return extracted IP or {@code unknown}
     */
    public static String extractIpFromLogLine(String line) {
        int start = line.indexOf(IP_ADDR_MARKER);
        if (start < 0) {
            return "unknown";
        }
        start += IP_ADDR_MARKER.length();
        String tail = line.substring(start);

        for (int end = tail.length(); end > 0; end--) {
            if (end < tail.length() && tail.charAt(end) != ':') {
                continue;
            }
            String candidate = tail.substring(0, end);
            if (isIpLiteral(candidate)) {
                return candidate;
            }
        }
        return "unknown";
    }
}

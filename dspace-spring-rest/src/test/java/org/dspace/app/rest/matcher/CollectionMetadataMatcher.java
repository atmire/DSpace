/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.matcher;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Matcher;

public class CollectionMetadataMatcher {

    private CollectionMetadataMatcher() { }

    public static Matcher<? super Object> matchTitle(String title) {
        return
                hasJsonPath("$.metadata[?(@.key=='dc.title')].value", contains(title))
        ;
    }

    public static Matcher<? super Object> matchMetadata(Map<String, String> metadata) {
        List<Matcher<? super Object>> matchers = new LinkedList<>();
        if (metadata != null) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                matchers.add(hasJsonPath("$.metadata[?(@.key=='" + entry.getKey() + "')].value",
                                         contains(entry.getValue())));
            }
        }
        return allOf(matchers);
    }

    public static Matcher<? super Object> matchMetadataNotInObject(Set<String> metadataFieldKeys) {
        List<Matcher<? super Object>> matchers = new LinkedList<>();
        if (metadataFieldKeys != null) {
            for (String metadataField : metadataFieldKeys) {
                matchers.add(hasNoJsonPath("$.metadata[?(@.key=='" + metadataField + "')].value"));
            }
        }
        return allOf(matchers);
    }
}

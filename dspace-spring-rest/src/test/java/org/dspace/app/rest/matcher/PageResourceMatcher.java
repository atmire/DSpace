/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.matcher;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import org.hamcrest.Matcher;

/**
 * Utility class to construct a Matcher for a Page object
 *
 */
public class PageResourceMatcher {

    private PageResourceMatcher() { }

    public static Matcher<? super Object> matchPageResource(UUID id, String name, String title, String language) {
        return allOf(
            hasJsonPath("$.id", is(id.toString())),
            hasJsonPath("$.name", is(name)),
            hasJsonPath("$.title", is(title)),
            hasJsonPath("$.language", is(language)),
            hasJsonPath("$.type", is("page")),
            hasJsonPath("$._links.content.href", containsString("/api/config/pages/" + id.toString() + "/content")),
            hasJsonPath("$._links.languages.href", containsString("/api/config/pages/" + id.toString() + "/languages")),
            hasJsonPath("$._links.self.href", containsString("/api/config/pages/" + id.toString()))
        );
    }


}

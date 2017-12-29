/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.matcher;

import org.hamcrest.Matcher;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

public class TaskMatcher {

    public static Matcher<? super Object> matchEntry(String name, String description){
        return allOf(
                hasJsonPath("$.name", is(name)),
                hasJsonPath("$.description", is(description))

        );
    }
}

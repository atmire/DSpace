package org.dspace.app.rest.matcher;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class SiteMatcher {

    public static Matcher<? super Object> matchEntry(){
        return allOf(
                hasJsonPath("$.uuid", Matchers.not(Matchers.empty())),
                hasJsonPath("$.name", Matchers.not(Matchers.empty())),
                hasJsonPath("$.handle", Matchers.not(Matchers.empty())),
                hasJsonPath("$.type", is("site")),
                hasJsonPath("$._links.self.href", containsString("/api/core/sites/"))

        );
    }
}

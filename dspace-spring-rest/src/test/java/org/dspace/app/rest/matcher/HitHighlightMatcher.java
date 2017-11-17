package org.dspace.app.rest.matcher;

import org.hamcrest.Matcher;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.*;

public class HitHighlightMatcher {

    public static Matcher<? super Object> entry(String value) {
        return allOf(
                hasJsonPath("$.*", contains(contains(containsString("Public"))))
        );
    }


}

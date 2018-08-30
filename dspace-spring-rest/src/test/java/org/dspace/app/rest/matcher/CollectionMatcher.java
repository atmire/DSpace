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

import java.util.Map;
import java.util.UUID;

import org.dspace.content.Bitstream;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class CollectionMatcher {

    private CollectionMatcher() { }

    public static Matcher<? super Object> matchCollectionEntry(String name, UUID uuid, String handle) {
        return matchCollectionEntry(name, uuid, handle, null, null);
    }


    public static Matcher<? super Object> matchCollectionEntry(String name, UUID uuid, String handle, Bitstream logo) {
        return matchCollectionEntry(name, uuid, handle, null, null);
    }

    public static Matcher<? super Object> matchCollectionEntry(String name, UUID uuid, String handle, Bitstream logo,
                                                               Map<String, String> metadata) {
        return allOf(
                hasJsonPath("$.uuid", is(uuid.toString())),
                hasJsonPath("$.name", is(name)),
                hasJsonPath("$.handle", is(handle)),
                hasJsonPath("$.type", is("collection")),
                hasJsonPath("$", Matchers.allOf(
                        CollectionMetadataMatcher.matchTitle(name),
                        CollectionMetadataMatcher.matchMetadata(metadata)
                )),
                matchLinks(uuid),
                matchLogo(logo)
        );
    }

    public static Matcher<? super Object> matchCollectionEntry(Map<String, String> nonExistingMetadata) {
        return allOf(
                hasJsonPath("$", CollectionMetadataMatcher.matchMetadataNotInObject(nonExistingMetadata.keySet())));
    }
    private static Matcher<? super Object> matchLinks(UUID uuid) {
        return allOf(
            hasJsonPath("$._links.logo.href", containsString("api/core/collections/" + uuid.toString() + "/logo")),
            hasJsonPath("$._links.self.href", containsString("api/core/collections/" + uuid.toString()))
        );
    }

    private static Matcher<? super Object> matchLogo(Bitstream logo) {
        return logo == null ?
            allOf(
                hasJsonPath("$._embedded.logo", Matchers.not(Matchers.empty()))
            ) :
            allOf(
                hasJsonPath("$._embedded.logo",
                        BitstreamMatcher.matchBitstreamEntry(logo.getID(), logo.getSizeBytes()))
            );
    }

}

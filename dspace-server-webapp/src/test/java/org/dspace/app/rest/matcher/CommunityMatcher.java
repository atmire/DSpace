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
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.UUID;

import org.dspace.content.Collection;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class CommunityMatcher {

    private CommunityMatcher() { }

    // Matcher for communities with no titles / no name
    // Since a name is simply the first title (see Community.java), we cannot use the matchers below
    public static Matcher<? super Object> matchCommunityEntry(UUID uuid, String handle) {
        return allOf(
                hasJsonPath("$.uuid", is(uuid.toString())),
                hasJsonPath("$.handle", is(handle)),
                hasJsonPath("$.type", is("community")),
                matchLinks(uuid)
        );
    }

    // Matcher for communities with multiple titles
    // The title metadata for communities with multiple titles contains a list, so the matchers below can't be used
    public static Matcher<? super Object> matchCommunityEntryMultipleTitles(List<String> titles, UUID uuid,
                                                                            String handle) {
        return allOf(
                hasJsonPath("$.uuid", is(uuid.toString())),
                hasJsonPath("$.name", is(titles.get(0))),
                hasJsonPath("$.handle", is(handle)),
                hasJsonPath("$.type", is("community")),
                matchLinks(uuid)
        );
    }

    public static Matcher<? super Object> matchCommunityEntry(String name, UUID uuid, String handle) {
        return allOf(
            matchProperties(name, uuid, handle),
            hasJsonPath("$._embedded.collections", Matchers.not(Matchers.empty())),
            matchLinks(uuid)
        );
    }

    public static Matcher<? super Object> matchProperties(String name, UUID uuid, String handle) {
        return allOf(
            hasJsonPath("$.uuid", is(uuid.toString())),
            hasJsonPath("$.name", is(name)),
            hasJsonPath("$.handle", is(handle)),
            hasJsonPath("$.type", is("community")),
            hasJsonPath("$.metadata", Matchers.allOf(
                MetadataMatcher.matchMetadata("dc.title", name)
            ))
        );
    }

    public static Matcher<? super Object> matchLinks(UUID uuid) {
        return allOf(
            hasJsonPath("$._links.collections.href",
                        Matchers.containsString("/api/core/communities/" + uuid.toString() + "/collections")),
            hasJsonPath("$._links.logo.href",
                        Matchers.containsString("/api/core/communities/" + uuid.toString() + "/logo")),
            hasJsonPath("$._links.self.href", Matchers.containsString("/api/core/communities/" + uuid.toString()))
        );
    }

    public static Matcher<? super Object> matchCommunityWithCollectionEntry(String name, UUID uuid, String handle,
                                                                            Collection col) {
        return allOf(
            matchProperties(name, uuid, handle),
            hasJsonPath("$._embedded.collections._embedded.collections[0]",
                        CollectionMatcher
                            .matchCollectionEntry(col.getName(), col.getID(), col.getHandle(), col.getLogo())),
            matchLinks(uuid)
        );
    }

}

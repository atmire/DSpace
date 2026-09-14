/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.service.AuthorityBackedRelationshipService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.utils.DSpace;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for the shared authority-backed relationship stamping helper
 * ({@link AuthorityBackedRelationshipService#markRelationshipForResolvedAuthority}).
 *
 * Covers ticket 04 (shared minting helper).
 *
 * @author Adamo Fapohunda (adamo.fapohunda at 4science.com)
 * @author Vincenzo Mecca (vins01-4science - vincenzo.mecca at 4science.com)
 */
public class AuthorityBackedRelationshipServiceIT extends AbstractIntegrationTestWithDatabase {

    private AuthorityBackedRelationshipService authorityBackedRelationshipService;
    private ItemService itemService;

    private Collection collection;
    private Item owner;
    private Item target;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        authorityBackedRelationshipService = new DSpace().getServiceManager()
            .getServiceByName(AuthorityBackedRelationshipServiceImpl.class.getCanonicalName(),
                AuthorityBackedRelationshipService.class);
        itemService = ContentServiceFactory.getInstance().getItemService();

        context.turnOffAuthorisationSystem();

        Community community = CommunityBuilder.createCommunity(context)
            .withName("community")
            .build();

        collection = CollectionBuilder.createCollection(context, community)
            .withName("collection")
            .build();

        owner = ItemBuilder.createItem(context, collection)
            .withTitle("owner publication")
            .withAuthor("Smith, John")
            .build();

        target = ItemBuilder.createItem(context, collection)
            .withTitle("target person")
            .build();

        context.restoreAuthSystemState();
    }

    @Test
    public void testStampsOwningValueWithRelationshipSides() throws Exception {
        context.turnOffAuthorisationSystem();

        MetadataValue authorValue = getFirstAuthorValue(owner);
        assertThat(authorValue.isRelationshipBacked(), equalTo(false));
        String originalValue = authorValue.getValue();
        String originalAuthority = authorValue.getAuthority();

        boolean stamped = authorityBackedRelationshipService
            .markRelationshipForResolvedAuthority(context, owner, authorValue, target);

        context.restoreAuthSystemState();

        assertThat(stamped, equalTo(true));
        // Side convention (D21): owner -> left, target -> right
        assertThat(authorValue.isRelationshipBacked(), equalTo(true));
        assertThat(authorValue.getLeftItem(), equalTo(owner.getID()));
        assertThat(authorValue.getRightItem(), equalTo(target.getID()));
        // The helper never touches value or authority
        assertThat(authorValue.getValue(), equalTo(originalValue));
        assertThat(authorValue.getAuthority(), equalTo(originalAuthority));
    }

    @Test
    public void testSecondCallForSameValueIsNoOp() throws Exception {
        context.turnOffAuthorisationSystem();

        MetadataValue authorValue = getFirstAuthorValue(owner);

        boolean first = authorityBackedRelationshipService
            .markRelationshipForResolvedAuthority(context, owner, authorValue, target);
        boolean second = authorityBackedRelationshipService
            .markRelationshipForResolvedAuthority(context, owner, authorValue, target);

        context.restoreAuthSystemState();

        // Idempotent: the second call changes nothing
        assertThat(first, equalTo(true));
        assertThat(second, equalTo(false));
    }

    @Test
    public void testTwoDifferentValuesStampIndependently() throws Exception {
        // Option B: two different mdvs on the same owner->target pair are stamped independently.
        context.turnOffAuthorisationSystem();

        itemService.addMetadata(context, owner, "dc", "contributor", "editor", null, "Smith, John");
        itemService.update(context, owner);

        MetadataValue authorValue = getFirstAuthorValue(owner);
        MetadataValue editorValue =
            itemService.getMetadata(owner, "dc", "contributor", "editor", Item.ANY).get(0);

        boolean authorStamped = authorityBackedRelationshipService
            .markRelationshipForResolvedAuthority(context, owner, authorValue, target);
        boolean editorStamped = authorityBackedRelationshipService
            .markRelationshipForResolvedAuthority(context, owner, editorValue, target);

        context.restoreAuthSystemState();

        assertThat(authorStamped, equalTo(true));
        assertThat(editorStamped, equalTo(true));
        assertThat(authorValue.isRelationshipBacked(), equalTo(true));
        assertThat(editorValue.isRelationshipBacked(), equalTo(true));
    }

    private MetadataValue getFirstAuthorValue(Item item) {
        return itemService.getMetadata(item, "dc", "contributor", "author", Item.ANY).get(0);
    }
}

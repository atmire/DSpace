/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.CollectionConverter;
import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.converter.JsonPatchConverter;
import org.dspace.app.rest.converter.MetadataConverter;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.exception.RepositoryMethodNotImplementedException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.CollectionRest;
import org.dspace.app.rest.model.CommunityRest;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.hateoas.CollectionResource;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.app.rest.repository.patch.DSpaceObjectPatch;
import org.dspace.app.rest.repository.patch.ItemPatch;
import org.dspace.app.rest.utils.CollectionRestEqualityUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.util.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * This is the repository responsible to manage Item Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(CollectionRest.CATEGORY + "." + CollectionRest.NAME)
public class CollectionRestRepository extends DSpaceObjectRestRepository<Collection, CollectionRest> {

    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(CollectionRestRepository.class);

    @Autowired
    CommunityService communityService;

    @Autowired
    CollectionConverter converter;

    @Autowired
    MetadataConverter metadataConverter;

    @Autowired
    CollectionRestEqualityUtils collectionRestEqualityUtils;

    @Autowired
    private CollectionService cs;

    @Autowired
    private ItemConverter itemConverter;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ItemPatch itemPatch;

    @Autowired
    private BitstreamService bitstreamService;


    public CollectionRestRepository(CollectionService dsoService,
                                    CollectionConverter dsoConverter) {
        super(dsoService, dsoConverter, new DSpaceObjectPatch<CollectionRest>() {});
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'COLLECTION', 'READ')")
    public CollectionRest findOne(Context context, UUID id) {
        Collection collection = null;
        try {
            collection = cs.find(context, id);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (collection == null) {
            return null;
        }
        return dsoConverter.fromModel(collection);
    }

    @Override
    public Page<CollectionRest> findAll(Context context, Pageable pageable) {
        List<Collection> it = null;
        List<Collection> collections = new ArrayList<Collection>();
        int total = 0;
        try {
            total = cs.countTotal(context);
            it = cs.findAll(context, pageable.getPageSize(), pageable.getOffset());
            for (Collection c : it) {
                collections.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Page<CollectionRest> page = new PageImpl<Collection>(collections, pageable, total).map(dsoConverter);
        return page;
    }

    @SearchRestMethod(name = "findAuthorizedByCommunity")
    public Page<CollectionRest> findAuthorizedByCommunity(
            @Parameter(value = "uuid", required = true) UUID communityUuid, Pageable pageable) {
        Context context = obtainContext();
        List<Collection> it = null;
        List<Collection> collections = new ArrayList<Collection>();
        try {
            Community com = communityService.find(context, communityUuid);
            if (com == null) {
                throw new ResourceNotFoundException(
                        CommunityRest.CATEGORY + "." + CommunityRest.NAME + " with id: " + communityUuid
                        + " not found");
            }
            it = cs.findAuthorized(context, com, Constants.ADD);
            for (Collection c : it) {
                collections.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Page<CollectionRest> page = utils.getPage(collections, pageable).map(dsoConverter);
        return page;
    }

    @SearchRestMethod(name = "findAuthorized")
    public Page<CollectionRest> findAuthorized(Pageable pageable) {
        Context context = obtainContext();
        List<Collection> it = null;
        List<Collection> collections = new ArrayList<Collection>();
        try {
            it = cs.findAuthorizedOptimized(context, Constants.ADD);
            for (Collection c : it) {
                collections.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Page<CollectionRest> page = utils.getPage(collections, pageable).map(dsoConverter);
        return page;
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'COLLECTION', 'WRITE')")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                         Patch patch) throws AuthorizeException, SQLException {
        patchDSpaceObject(apiCategory, model, id, patch);
    }

    @Override
    public Class<CollectionRest> getDomainClass() {
        return CollectionRest.class;
    }

    @Override
    public CollectionResource wrapResource(CollectionRest collection, String... rels) {
        return new CollectionResource(collection, utils, rels);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    protected CollectionRest createAndReturn(Context context) throws AuthorizeException {
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        CollectionRest collectionRest;
        try {
            ServletInputStream input = req.getInputStream();
            collectionRest = mapper.readValue(input, CollectionRest.class);
        } catch (IOException e1) {
            throw new UnprocessableEntityException("Error parsing request body: " + e1.toString());
        }

        Collection collection;


        String parentCommunityString = req.getParameter("parent");
        try {
            Community parent = null;
            if (StringUtils.isNotBlank(parentCommunityString)) {

                UUID parentCommunityUuid = UUIDUtils.fromString(parentCommunityString);
                if (parentCommunityUuid == null) {
                    throw new DSpaceBadRequestException("The given parent was invalid: "
                            + parentCommunityString);
                }

                parent = communityService.find(context, parentCommunityUuid);
                if (parent == null) {
                    throw new UnprocessableEntityException("Parent community for id: "
                            + parentCommunityUuid + " not found");
                }
            } else {
                throw new DSpaceBadRequestException("The parent parameter cannot be left empty," +
                                                  "collections require a parent community.");
            }
            collection = cs.create(context, parent);
            cs.update(context, collection);
            metadataConverter.setMetadata(context, collection, collectionRest.getMetadata());
        } catch (SQLException e) {
            throw new RuntimeException("Unable to create new Collection under parent Community " +
                                           parentCommunityString, e);
        }
        return converter.convert(collection);
    }


    @Override
    @PreAuthorize("hasPermission(#id, 'COLLECTION', 'WRITE')")
    protected CollectionRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                JsonNode jsonNode)
        throws RepositoryMethodNotImplementedException, SQLException, AuthorizeException {
        CollectionRest collectionRest;
        try {
            collectionRest = new ObjectMapper().readValue(jsonNode.toString(), CollectionRest.class);
        } catch (IOException e) {
            throw new UnprocessableEntityException("Error parsing collection json: " + e.getMessage());
        }
        Collection collection = cs.find(context, id);
        if (collection == null) {
            throw new ResourceNotFoundException(apiCategory + "." + model + " with id: " + id + " not found");
        }
        CollectionRest originalCollectionRest = converter.fromModel(collection);
        if (collectionRestEqualityUtils.isCollectionRestEqualWithoutMetadata(originalCollectionRest, collectionRest)) {
            metadataConverter.setMetadata(context, collection, collectionRest.getMetadata());
        } else {
            throw new IllegalArgumentException("The UUID in the Json and the UUID in the url do not match: "
                                                   + id + ", "
                                                   + collectionRest.getId());
        }
        return converter.fromModel(collection);
    }
    @Override
    @PreAuthorize("hasPermission(#id, 'COLLECTION', 'DELETE')")
    protected void delete(Context context, UUID id) throws AuthorizeException {
        try {
            Collection collection = getCollection(context, id);
            cs.delete(context, collection);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to delete Collection with id = " + id, e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to delete collection because the logo couldn't be deleted", e);
        }
    }

    public Bitstream setLogo(Context context, UUID uuid, MultipartFile uploadfile)
            throws IOException, AuthorizeException, SQLException {

        Collection collection = null;
        try {
            collection = cs.find(context, uuid);
        } catch (SQLException e) {
            log.error("Something went wrong trying to find the collection with uuid: " + uuid, e);
        }

        if (collection == null) {
            throw new ResourceNotFoundException(
                    "The given uuid did not resolve to a collection on the server: " + uuid);
        }

        Bitstream bitstream;
        if (uploadfile != null) {
            if (collection.getLogo() != null) {
                throw new UnprocessableEntityException(
                        "The collection with the given uuid already has a logo: " + uuid);
            }
            bitstream = cs.setLogo(context, collection, uploadfile.getInputStream());
        } else {
            if (collection.getLogo() == null) {
                throw new UnprocessableEntityException(
                        "The collection with the given uuid didn't have a logo: " + uuid);
            }
            bitstream = collection.getLogo();
            cs.setLogo(context, collection, null);
        }

        cs.update(context, collection);
        bitstreamService.update(context, bitstream);
        context.complete();

        return bitstream;
    }

    public Bitstream removeLogo(Context context, UUID uuid) throws SQLException, IOException, AuthorizeException {
        return setLogo(context, uuid, null);
    }

    public Bitstream updateLogo(Context context, UUID uuid, MultipartFile uploadfile)
            throws SQLException, IOException, AuthorizeException {

        Collection collection = null;
        try {
            collection = cs.find(context, uuid);
        } catch (SQLException e) {
            log.error("Something went wrong trying to find the collection with uuid: " + uuid, e);
        }

        if (collection == null) {
            throw new ResourceNotFoundException(
                    "The given uuid did not resolve to a collection on the server: " + uuid);
        }

        if (collection.getLogo() == null) {
            throw new UnprocessableEntityException("The collection with the given uuid didn't have a logo: " + uuid);
        }
        Bitstream oldBitstream = collection.getLogo();
        Bitstream newBitstream = cs.setLogo(context, collection, uploadfile.getInputStream());

        cs.update(context, collection);
        bitstreamService.update(context, newBitstream);
        bitstreamService.update(context, oldBitstream);
        context.complete();

        return newBitstream;
    }

    public ItemRest createTemplateItem(Context context, UUID uuid) throws SQLException, AuthorizeException {
        Collection collection = getCollection(context, uuid);

        if (collection.getTemplateItem() != null) {
            throw new UnprocessableEntityException("Collection with ID " + uuid + " already contains a template item");
        }

        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ItemRest inputItemRest;
        try {
            ServletInputStream input = req.getInputStream();
            inputItemRest = mapper.readValue(input, ItemRest.class);
        } catch (IOException e1) {
            throw new UnprocessableEntityException("Error parsing request body", e1);
        }

        if (inputItemRest.getInArchive() || inputItemRest.getDiscoverable() || inputItemRest.getWithdrawn()) {
            throw new UnprocessableEntityException(
                    "The template item should not be archived, discoverable or withdrawn");
        }

        cs.createTemplateItem(context, collection);
        Item templateItem = collection.getTemplateItem();
        metadataConverter.setMetadata(context, templateItem, inputItemRest.getMetadata());
        templateItem.setDiscoverable(false);

        cs.update(context, collection);
        itemService.update(context, templateItem);
        context.commit();

        return itemConverter.fromModel(templateItem);
    }

    public ItemRest getTemplateItem(Context context, UUID uuid) throws SQLException {
        Collection collection = getCollection(context, uuid);

        Item item = collection.getTemplateItem();
        if (item == null) {
            throw new ResourceNotFoundException(
                    "TemplateItem from " + CollectionRest.CATEGORY + "." + CollectionRest.NAME + " with id: "
                            + uuid + " not found");
        }

        return itemConverter.fromModel(item);
    }

    public ItemRest patchTemplateItem(Context context, UUID uuid, JsonNode jsonNode)
            throws SQLException, AuthorizeException {
        Collection collection = getCollection(context, uuid);

        Item item = collection.getTemplateItem();
        if (item == null) {
            throw new UnprocessableEntityException(
                    "TemplateItem from " + CollectionRest.CATEGORY + "." + CollectionRest.NAME + " with id: "
                            + uuid + " not found");
        }

        JsonPatchConverter patchConverter = new JsonPatchConverter(mapper);
        Patch patch = patchConverter.convert(jsonNode);
        for (Operation operation : patch.getOperations()) {
            if (operation.getPath().equals("/inArchive")
                    || operation.getPath().equals("/discoverable")
                    || operation.getPath().equals("/withdrawn")) {
                throw new UnprocessableEntityException(
                        "The template item should not be archived, discoverable or withdrawn."
                                + " Therefore editing these values is not allowed.");
            }
        }

        ItemRest patchedItemRest = itemPatch.patch(itemConverter.fromModel(item), patch.getOperations());
        if (!itemConverter.fromModel(item).getMetadata().equals(patchedItemRest.getMetadata())) {
            metadataConverter.setMetadata(obtainContext(), item, patchedItemRest.getMetadata());
        }
        context.commit();

        return itemConverter.fromModel(item);
    }

    public void removeTemplateItem(Context context, UUID uuid) throws SQLException, IOException, AuthorizeException {
        Collection collection = getCollection(context, uuid);

        Item item = collection.getTemplateItem();
        if (item == null) {
            throw new UnprocessableEntityException(
                    "TemplateItem from " + CollectionRest.CATEGORY + "." + CollectionRest.NAME + " with id: "
                            + uuid + " not found");
        }

        cs.removeTemplateItem(context, collection);
        cs.update(context, collection);
        context.commit();
    }

    private Collection getCollection(Context context, UUID uuid) throws SQLException {
        Collection collection = cs.find(context, uuid);
        if (collection == null) {
            throw new ResourceNotFoundException(
                    CollectionRest.CATEGORY + "." + CollectionRest.NAME + " with id: " + uuid + " not found");
        }
        return collection;
    }
}

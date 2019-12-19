/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.rest.converter.MetadataSuggestionEntryConverter;
import org.dspace.app.rest.converter.MetadataSuggestionsSourceRestConverter;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.model.MetadataSuggestionEntryRest;
import org.dspace.app.rest.model.MetadataSuggestionsSourceRest;
import org.dspace.app.rest.model.hateoas.DSpaceResource;
import org.dspace.app.rest.model.hateoas.MetadataSuggestionsSourceResource;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Context;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.provider.metadata.MetadataSuggestionProvider;
import org.dspace.external.provider.metadata.service.MetadataSuggestionProviderService;
import org.dspace.external.provider.metadata.service.impl.MetadataItemSuggestions;
import org.dspace.services.RequestService;
import org.dspace.workflow.WorkflowItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.PagedResources;
import org.springframework.stereotype.Component;

/**
 * This is a repository dealing with MetadataSuggestion objects and its methods
 */
@Component(MetadataSuggestionsSourceRest.CATEGORY + "." + MetadataSuggestionsSourceRest.NAME)
public class MetadataSuggestionsRestRepository extends DSpaceRestRepository<MetadataSuggestionsSourceRest, String> {

    private static final Logger log = Logger.getLogger(ItemRestRepository.class);

    @Autowired
    private MetadataSuggestionProviderService metadataSuggestionProviderService;

    @Autowired
    private MetadataSuggestionEntryConverter metadataSuggestionEntryConverter;

    @Autowired
    private MetadataSuggestionsSourceRestConverter metadataSuggestionsSourceRestConverter;

    @Autowired
    private WorkflowItemService workflowItemService;

    @Autowired
    private WorkspaceItemService workspaceItemService;

    /**
     * This method will create a page of MetadataSuggestionsSources that adheres to the given parameters
     * @param pageable              The pageable object for this request
     * @param inProgressSubmission  The InProgressionSubmission object for this call
     * @return                      A page containing MetadataSuggestionsSources
     */
    public Page<MetadataSuggestionsSourceRest> getAllMetadataSuggestionSources(Pageable pageable,
        InProgressSubmission inProgressSubmission) {

        if (inProgressSubmission == null) {
            throw new DSpaceBadRequestException("A valid workflowItem or workspaceItem needs to be passed along in" +
                                                    " the request parameters");
        }
        List<MetadataSuggestionProvider> metadataSuggestionProviders = metadataSuggestionProviderService
            .getMetadataSuggestionProviders(inProgressSubmission);
        Page<MetadataSuggestionsSourceRest> page = utils.getPage(metadataSuggestionProviders, pageable)
                                                        .map(metadataSuggestionsSourceRestConverter);

        return page;
    }

    /**
     * This method constructs a {@link MetadataSuggestionEntryRest} object based on the given parameter
     * @param suggestionName    The name of the MetadataSuggestionProvider to be used
     * @param entryId           The ID of the entry to be looked up in the relevant MetadataSuggestionProvider
     * @param inProgressSubmission  The InProgressSubmission to be used
     * @return
     */
    public MetadataSuggestionEntryRest getMetadataSuggestionEntry(String suggestionName, String entryId, InProgressSubmission inProgressSubmission) {
        //TODO Save Workspaceitem/workflowitemid in rest object so we can use it in the linkfactory
        MetadataItemSuggestions metadataItemSuggestions = metadataSuggestionProviderService.getMetadataItemSuggestions(suggestionName, entryId, inProgressSubmission);
        return metadataSuggestionEntryConverter.fromModel(metadataItemSuggestions);
    }

    @Override
    public MetadataSuggestionsSourceRest findOne(Context context, String id) {
        MetadataSuggestionProvider metadataSuggestionProvider = metadataSuggestionProviderService
            .getMetadataSuggestionProvider(id);
        if (metadataSuggestionProvider == null) {
            throw new ResourceNotFoundException("MetadataSuggestionProvider for: " + id + " couldn't be found");
        }
        return metadataSuggestionsSourceRestConverter.fromModel(metadataSuggestionProvider);
    }

    @Override
    public Page<MetadataSuggestionsSourceRest> findAll(Context context, Pageable pageable) {
        HttpServletRequest httpServletRequest = requestService.getCurrentRequest().getHttpServletRequest();
        String workspaceItemIdString = httpServletRequest.getParameter("workspaceitem");
        Integer workspaceItemId = null;
        if (StringUtils.isNotBlank(workspaceItemIdString)) {
            workspaceItemId = Integer.parseInt(workspaceItemIdString);
        }
        String workflowItemIdString = httpServletRequest.getParameter("workflowitem");
        Integer workflowItemId = null;
        if (StringUtils.isNotBlank(workflowItemIdString)) {
            workflowItemId = Integer.parseInt(workflowItemIdString);
        }
        InProgressSubmission inProgressSubmission = resolveInProgressSubmission(workspaceItemId, workflowItemId,
                                                                                context);
        Page<MetadataSuggestionsSourceRest> metadataSuggestionsSourceRestPage =
            getAllMetadataSuggestionSources(pageable, inProgressSubmission);
        return metadataSuggestionsSourceRestPage;
    }

    public InProgressSubmission resolveInProgressSubmission(Integer workspaceItemId, Integer workflowItemId,
                                                            Context context) {
        InProgressSubmission inProgressSubmission = null;
        try {
            if (workflowItemId != null) {
                inProgressSubmission = workflowItemService.find(context, workflowItemId);
            } else if (workspaceItemId != null) {
                inProgressSubmission = workspaceItemService.find(context, workspaceItemId);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return inProgressSubmission;
    }

    public Class<MetadataSuggestionsSourceRest> getDomainClass() {
        return MetadataSuggestionsSourceRest.class;
    }

    public DSpaceResource<MetadataSuggestionsSourceRest> wrapResource(MetadataSuggestionsSourceRest model,
                                                                      String... rels) {
        return new MetadataSuggestionsSourceResource(model, utils, rels);
    }
}

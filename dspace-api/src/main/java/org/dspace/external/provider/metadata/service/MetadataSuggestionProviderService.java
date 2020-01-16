/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.external.provider.metadata.service;

import java.util.List;
import java.util.Optional;

import org.dspace.content.InProgressSubmission;
import org.dspace.external.provider.metadata.MetadataSuggestionProvider;
import org.dspace.external.provider.metadata.service.impl.MetadataItemSuggestions;
import org.dspace.external.provider.metadata.service.impl.MetadataSuggestionDifferences;

/**
 * This is the interface for the MetadataSuggestionService implementation
 */
public interface MetadataSuggestionProviderService {

    /**
     * This method will return a list of MetadataSuggestionProvider objects that support the given InProgressSubmission
     * @param inProgressSubmission  The given InProgressSubmission
     * @return The list of MetadataSuggestionProvider objects that support the given parameter
     */
    List<MetadataSuggestionProvider> getMetadataSuggestionProviders(InProgressSubmission inProgressSubmission);

    /**
     * This method will return the MetadataSuggestionProvider that has the given id
     * @param id    The id of the MetadataSuggestionProvider that is to be returned
     * @return The MetadataSuggestionProvider that has the given id as its actual id
     */
    Optional<MetadataSuggestionProvider> getMetadataSuggestionProvider(String id);

    /**
     * This method will return a {@link MetadataItemSuggestions} object based on the given parameters
     * @param suggestionName        The name of the MetadataSuggestionProvider to be used
     * @param entryId               The ID of the entry to be searched for in the relevant MetadataSuggestionProvider
     * @param inProgressSubmission  The InProgressSubmission for this suggestion
     * @return The MetadataItemSuggestion
     */
    Optional<MetadataItemSuggestions> getMetadataItemSuggestions(String suggestionName, String entryId,
                                                                 InProgressSubmission inProgressSubmission);

    /**
     * This method will construct a MetadataSuggestionDifferences object wrapped in an optional from the given
     * SuggestionName service with the given ID and the InProgressSubmission's metadata.
     * It will retrieve the metadata from the given service and compare this with the metadata from the
     * InProgressSubmission and it'll construct the MetadataSuggestionDifferences object based on that
     * @param suggestionName        The name for the service to be used
     * @param entryId               The id of the entry to be queried in the service
     * @param inProgressSubmission  The InProgressSubmission to be used for the current metadata
     * @return                      The constructed MetadataSuggestionDifferences object
     */
    public Optional<MetadataSuggestionDifferences> getMetadataSuggestionDifferences(String suggestionName,
        String entryId, InProgressSubmission inProgressSubmission);
}

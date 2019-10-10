package org.dspace.content.authority.service;

import org.dspace.content.authority.ChoiceAuthority;
import org.dspace.external.model.ExternalDataObject;

public interface CacheableChoiceAuthority extends ChoiceAuthority {

    void cacheAuthorityValue(String metadataField, ExternalDataObject externalDataObject);

}

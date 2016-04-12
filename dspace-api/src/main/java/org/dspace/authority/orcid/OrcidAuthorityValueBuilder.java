package org.dspace.authority.orcid;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.PersonAuthorityValueBuilder;
import org.dspace.authority.orcid.model.Bio;
import org.dspace.authority.orcid.model.BioExternalIdentifier;
import org.dspace.authority.orcid.model.BioName;
import org.dspace.authority.orcid.model.BioResearcherUrl;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/16
 * Time: 15:45
 */
public class OrcidAuthorityValueBuilder extends PersonAuthorityValueBuilder<OrcidAuthorityValue>
{
    protected OrcidConnector orcidConnector;

    @Override
    public OrcidAuthorityValue buildAuthorityValue(String identifier, String content)
    {
        Bio orcidBio = orcidConnector.queryAuthorityID(identifier);
        OrcidAuthorityValue orcidAuthorityValue = buildAuthorityValueFromOrcidBio(orcidBio);
        if (orcidAuthorityValue != null)
        {
            return orcidAuthorityValue;
        }


        return null;
    }

    protected OrcidAuthorityValue buildAuthorityValueFromOrcidBio(Bio orcidBio) {
        //Make sure we have a result
        if(orcidBio != null)
        {
            OrcidAuthorityValue orcidAuthorityValue = buildAuthorityValue();
            orcidAuthorityValue.updateLastModifiedDate();
            orcidAuthorityValue.setCreationDate(new Date());
            storeOrcidBioInAuthorityValue(orcidAuthorityValue, orcidBio);
            return orcidAuthorityValue;
        }
        return null;
    }

    @Override
    public OrcidAuthorityValue buildAuthorityValue(SolrDocument document)
    {
        OrcidAuthorityValue orcidAuthorityValue = super.buildAuthorityValue(document);
        orcidAuthorityValue.setOrcid_id(String.valueOf(document.getFieldValue("orcid_id")));
        for (String fieldName : document.getFieldNames()) {
            String labelPrefix = "label_";
            if (fieldName.startsWith(labelPrefix))
            {
                String label = fieldName.substring(labelPrefix.length());
                Collection<Object> fieldValues = document.getFieldValues(fieldName);
                for (Object o : fieldValues) {
                    orcidAuthorityValue.addOtherMetadata(label, String.valueOf(o));
                }
            }
        }
        return orcidAuthorityValue;
    }

    @Override
    public OrcidAuthorityValue buildAuthorityValue() {
        return new OrcidAuthorityValue();
    }

    @Override
    public List<AuthorityValue> buildAuthorityValueFromExternalSource(String text, int max) {
        final List<Bio> bios = orcidConnector.queryBio(text, max);
        List<AuthorityValue> result = new ArrayList<>();
        for (Bio bio : bios)
        {
            final OrcidAuthorityValue orcidAuthorityValue = buildAuthorityValueFromOrcidBio(bio);
            if(orcidAuthorityValue != null)
            {
                result.add(orcidAuthorityValue);
            }
        }
        return result;
    }

    /**
     * Method is responsible
     * @param bio
     * @return
     */
    protected void storeOrcidBioInAuthorityValue(OrcidAuthorityValue authorityValue, Bio bio) {
        BioName name = bio.getName();

        if (!StringUtils.equals(bio.getOrcid(), authorityValue.getOrcid_id())) {
            authorityValue.setOrcid_id(bio.getOrcid());
        }

        if (!StringUtils.equals(name.getFamilyName(), authorityValue.getLastName())) {
            authorityValue.setLastName(name.getFamilyName());
        }

        if (!StringUtils.equals(name.getGivenNames(), authorityValue.getFirstName())) {
            authorityValue.setFirstName(name.getGivenNames());
        }

        if (StringUtils.isNotBlank(name.getCreditName())) {
            if (!authorityValue.getNameVariants().contains(name.getCreditName())) {
                authorityValue.addNameVariant(name.getCreditName());
            }
        }
        for (String otherName : name.getOtherNames()) {
            if (!authorityValue.getNameVariants().contains(otherName)) {
                authorityValue.addNameVariant(otherName);
            }
        }

        if (authorityValue.isNewMetadata("country", bio.getCountry())) {
            authorityValue.addOtherMetadata("country", bio.getCountry());
        }

        for (String keyword : bio.getKeywords()) {
            if (authorityValue.isNewMetadata("keyword", keyword)) {
                authorityValue.addOtherMetadata("keyword", keyword);
            }
        }

        for (BioExternalIdentifier externalIdentifier : bio.getBioExternalIdentifiers()) {
            if (authorityValue.isNewMetadata("external_identifier", externalIdentifier.toString())) {
                authorityValue.addOtherMetadata("external_identifier", externalIdentifier.toString());

            }
        }

        for (BioResearcherUrl researcherUrl : bio.getResearcherUrls()) {
            if (authorityValue.isNewMetadata("researcher_url", researcherUrl.toString())) {
                authorityValue.addOtherMetadata("researcher_url", researcherUrl.toString());
            }
        }

        if (authorityValue.isNewMetadata("biography", bio.getBiography())) {
            authorityValue.addOtherMetadata("biography", bio.getBiography());
        }

        //TODO: is thid needed ?
        authorityValue.setValue(authorityValue.getName());
    }

    public OrcidConnector getOrcidConnector() {
        return orcidConnector;
    }

    @Required
    public void setOrcidConnector(OrcidConnector orcidConnector) {
        this.orcidConnector = orcidConnector;
    }
}

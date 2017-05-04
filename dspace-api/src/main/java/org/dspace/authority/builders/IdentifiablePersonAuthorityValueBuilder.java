/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.builders;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.factory.IdentifiableAuthorityValueBuilder;
import org.dspace.authority.orcid.OrcidAuthorityValue;
import org.dspace.authority.orcid.OrcidConnector;
import org.dspace.utils.DSpace;
import org.orcid.jaxb.model.record_v2.*;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * OrcidAuthorityValueBuilder handles the creation of OrcidAuthorityValues.
 *
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 */
public class IdentifiablePersonAuthorityValueBuilder extends PersonAuthorityValueBuilder<OrcidAuthorityValue> implements IdentifiableAuthorityValueBuilder
{
    protected OrcidConnector orcidConnector = new DSpace().getServiceManager().getServiceByName("orcidConnector", OrcidConnector.class);

    /**
     * Build an authority value with the provided orcid identifier
     * @param identifier
     * the orcid identifier
     * @return
     * the resulting OrcidAuthorityValue
     */
    @Override
    public OrcidAuthorityValue buildAuthorityValue(String identifier, String content)
    {
        Person orcidBio = orcidConnector.queryAuthorityID(identifier);
        OrcidAuthorityValue orcidAuthorityValue = buildAuthorityValueFromOrcidBio(orcidBio);
        if (orcidAuthorityValue != null)
        {
            return orcidAuthorityValue;
        }


        return null;
    }

    /**
     * Build an authority value with the provided orcid bio
     * @param orcidBio
     * The orcid bio of the authority value
     * @return
     * The created authority value
     */
    protected OrcidAuthorityValue buildAuthorityValueFromOrcidBio(Person orcidBio) {
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

    /**
     * Build an authority value with the provided solr document. Only orcid authority specific fields are handled in this method,
     * the super method is used to set General fields.
     * @param document
     * The solr document of the authority value
     * @return
     * The created authority value
     */
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
        final List<Person> bios = orcidConnector.queryBio(text, max);
        List<AuthorityValue> result = new ArrayList<>();
        for (Person bio : bios) {
            final OrcidAuthorityValue orcidAuthorityValue = buildAuthorityValueFromOrcidBio(bio);
            if(orcidAuthorityValue != null)
            {
                result.add(orcidAuthorityValue);
            }
        }
        return result;
    }

    /**
     * Add the information from an orcid bio to an authority value
     * @param authorityValue
     * the authority value the information is added to
     * @param bio
     * the orcid bio
     */
    protected void storeOrcidBioInAuthorityValue(OrcidAuthorityValue authorityValue, Person bio) {
        Name name = bio.getName();

        if (!StringUtils.equals(name.getPath(), authorityValue.getOrcid_id())) {
            authorityValue.setOrcid_id(name.getPath());
        }

        if (!StringUtils.equals(name.getFamilyName().getContent(), authorityValue.getLastName())) {
            authorityValue.setLastName(name.getFamilyName().getContent());
        }

        if (!StringUtils.equals(name.getGivenNames().getContent(), authorityValue.getFirstName())) {
            authorityValue.setFirstName(name.getGivenNames().getContent());
        }

        if (name.getCreditName() != null && StringUtils.isNotBlank(name.getCreditName().getContent())) {
            if (!authorityValue.getNameVariants().contains(name.getCreditName())) {
                authorityValue.addNameVariant(name.getCreditName().getContent());
            }
        }


//        if (authorityValue.isNewMetadata("country", bio.getCountry())) {
//            authorityValue.addOtherMetadata("country", bio.getCountry());
//        }

        if (bio.getKeywords() != null) {
            for (Keyword keyword : bio.getKeywords().getKeywords()) {
                if (authorityValue.isNewMetadata("keyword", keyword.getContent())) {
                    authorityValue.addOtherMetadata("keyword", keyword.getContent());
                }
            }
        }

        PersonExternalIdentifiers externalIdentifiers = bio.getExternalIdentifiers();
        if (externalIdentifiers != null) {
            for (PersonExternalIdentifier externalIdentifier : externalIdentifiers.getExternalIdentifiers()) {
                if (authorityValue.isNewMetadata("external_identifier", externalIdentifier.toString())) {
                    authorityValue.addOtherMetadata("external_identifier", externalIdentifier.toString());

                }
            }
        }
        if (bio.getResearcherUrls() != null) {
            for (ResearcherUrl researcherUrl : bio.getResearcherUrls().getResearcherUrls()) {
                if (authorityValue.isNewMetadata("researcher_url", researcherUrl.toString())) {
                    authorityValue.addOtherMetadata("researcher_url", researcherUrl.toString());
                }
            }

        }
        if (bio.getBiography() != null) {
            if (authorityValue.isNewMetadata("biography", bio.getBiography().getContent())) {
                authorityValue.addOtherMetadata("biography", bio.getBiography().getContent());
            }
        }

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

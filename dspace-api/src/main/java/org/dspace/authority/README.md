# How to reuse this functionality for other metadata fields.
Let's say dc.relation.ispartofseries is a *onebox* input field labeled 'Journals' in the submission and needs to have an authority look-up just like dc.contributor.author.

Additionally the journal document should contain:
* journal title: mandatory, not repeatable
* ISSN: optional, repeatable
* Publisher: optional, not repeatable
* an internal ID

## Add the authority controlled metadata field to dspace.cfg
```
choices.plugin.dc.relation.ispartofseries = SolrAuthorAuthority
choices.presentation.dc.relation.ispartofseries = lookup
authority.controlled.dc.relation.ispartofseries = true

authority.author.indexer.field.1=dc.contributor.author
authority.author.indexer.field.2=dc.relation.ispartofseries
```

## Add the desired properties of the new authority type in the solr schema
solr/authority/conf/schema.xml
```
     <!-- journal-->
     <field name="issn" type="string" multiValued="true" indexed="true" stored="true" required="false"/>
     <field name="publisher" type="string" multiValued="false" indexed="true" stored="true" required="false"/>
 </fields>
```
The title and the internal ID find their places in the already existing "value" and "id" fields.

## Extend org.dspace.authority.AuthorityValue, add the fields and implement the methods
```
public class JournalAuthorityValue extends AuthorityValue {

    protected String publisher;
    protected List<String> ISSN = new ArrayList<String>();
```
Since the journal title is to be stored as the record's value no specific instance variable is needed, **AuthorityValue** already provides this.

Override **getId()** to control how the authority ID is generated.
```
   @Override
    public String getId() {
        String nonDigestedIdentifier = JournalAuthorityValue.class.toString() + "field: " + getField() +  "issn: " + issn + ", publisher: " + publisher;
        // We return an md5 digest of the toString, this will ensure a unique identifier for the same value each time
        return DigestUtils.md5Hex(nonDigestedIdentifier);
    }
```

Override **getSolrInputDocument()** to control what is stored in the solr document.

```
    @Override
    public SolrInputDocument getSolrInputDocument() {
        SolrInputDocument doc = super.getSolrInputDocument();
        doc.addField("publisher", getPublisher());
        for (String issn : ISSN) {
            doc.addField("ISSN", issn);
        }
        return doc;
    }
```

Override **choiceSelectMap()** to control what will be displayed in the lookup UI.
```
    @Override
    public Map<String, String> choiceSelectMap() {
        Map<String, String> map = super.choiceSelectMap();
        if (StringUtils.isNotBlank(getValue())) {
            map.put("Title", getValue());
        }
        String issn = "";
        for (String s : ISSN) {
            if (StringUtils.isNotBlank(s)) {
                issn += s + " ";
            }
        }
        if (StringUtils.isNotBlank(issn)) {
            map.put("issn", issn.trim());
        }
        if (StringUtils.isBlank(publisher)) {
            map.put("publisher", publisher);
        }
        return map;
    }
```
Override **getAuthorityType()**, **generateString()** and make sure they are consistent.

* **getAuthorityType()** The authority type is an implicit field in the solr document and is necessary to cast the solr document into the correct java class.
* **generateString()** is a temporary value for the metadata's authority that will be handed to the authority consumer. This is only used when an external authority is chosen that has not yet been added to the solr cache. It needs to contain enough information to make an inambiguous external lookup, e.g. some sort of id.

```
    @Override
    public String getAuthorityType() {
        return "journal";
    }

        @Override
        public String generateString() {
            return new AuthorityKeyRepresentation(getAuthorityType(), getId()).toString();
        }
```

Override **hasTheSameInformationAs(Object o)** and include only the sensible fields. The use case for this method is an update from the external information source. When comparing a value before and after the update and returning false, the last-modified-date will be updated.

## Extend org.dspace.authority.factory.AuthorityValueBuilder and implement the methods

```
public class PersonAuthorityValueBuilder<T extends PersonAuthorityValue> extends AuthorityValueBuilder<T> {
```

Override **buildAuthorityValue()** methods. These methods will be used to build JournalAuthorityValues. 

```
   @Override
    public T buildAuthorityValue() {
        return (T) new JournalAuthorityValue();
    }

    @Override
    public T buildAuthorityValue(String identifier, String content)
    {
        final T authorityValue = buildAuthorityValue();
        authorityValue.setValue(content);
        return authorityValue;
    }
    
    @Override
    public T buildAuthorityValue(SolrDocument document)
    {
        T authorityValue = super.buildAuthorityValue(document);
        authorityValue.setPublisher(ObjectUtils.toString(document.getFieldValue("publisher")));
        authorityValue.setIssn(ObjectUtils.toString(document.getFieldValue("issn")));
        return authorityValue;
    }
```

## Add the new classes to the spring configuration
config/spring/api/orcid-authority-services.xml

```
        <bean id="authorityValueFactory" class="org.dspace.authority.factory.AuthorityValueFactoryImpl">
            <property name="authorityValueBuilders">
                <util:map map-class="java.util.LinkedHashMap">
                    <entry key="#{T(org.dspace.authority.orcid.OrcidAuthorityValue).TYPE}" value-ref="org.dspace.authority.orcid.OrcidAuthorityValueBuilder"/>
                    <entry key="#{T(org.dspace.authority.PersonAuthorityValue).TYPE}" value-ref="org.dspace.authority.PersonAuthorityValueBuilder"/>
                    <entry key="#{T(org.dspace.authority.JournalAuthorityValue).TYPE}" value-ref="org.dspace.authority.JournalAuthorityValueBuilder"/>
                </util:map>
            </property>
            <property name="authorityValueBuilderDefaults">
                <map>
                    <entry key="dc_contributor_author">
                        <ref bean="org.dspace.authority.PersonAuthorityValueBuilder"/>
                    </entry>
                </map>
            </property>
        </bean>
        
            <bean id="org.dspace.authority.JournalAuthorityValueBuilder" class="org.dspace.authority.JournalAuthorityValueBuilder">
                <property name="metadataFields">
                    <list>
                        <value>dc_relation_ispartofseries</value>
                    </list>
                </property>
            </bean>
```


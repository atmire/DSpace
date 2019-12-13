<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : merge-submission-forms.xsl
    Created on : Dec 13, 2019
    Author     : ben . bosman @ atmire . com
    Description: Merges the main submission-forms.xml with another
                 submission-forms.xml file to copy all new forms, value pairs, fields, …
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

    <xsl:output method="xml" doctype-system="submission-forms.dtd" indent="yes" />

    <xsl:param name="fileName" select="fileName" />
<!--    <xsl:param name="fileName" select="'journal-submission-forms.xml'" />-->
    <xsl:param name="updates" select="document($fileName)" />

    <xsl:variable name="updateForms" select="$updates/input-forms/form-definitions/form" />
    <xsl:variable name="originalForms" select="/input-forms/form-definitions/form" />

    <xsl:variable name="updateValuePairs" select="$updates/input-forms/form-value-pairs/value-pairs" />
    <xsl:variable name="originalValuePairs" select="/input-forms/form-value-pairs/value-pairs" />

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/input-forms/form-definitions">
        <xsl:copy>
<!--            <xsl:apply-templates select="@* | node()[not(self::entry)] |-->
<!--                                   entry[not(@name = $updateForms/@name)]" />-->
            <xsl:text>&#xa;        </xsl:text>
            <xsl:apply-templates select="form" />
<!--            <xsl:apply-templates select="$updateForms" />-->
            <xsl:text>&#xa;        </xsl:text>
            <xsl:apply-templates select="$updateForms[not(@name = $originalForms/@name)]" mode="new" />
            <xsl:text>&#xa;    </xsl:text>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/input-forms/form-definitions/form">
        <xsl:variable name="name" select="@name"/>
        <xsl:copy>
            <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
<!--            <xsl:apply-templates select="@* | node()[not(self::entry)] |-->
<!--                                   entry[not(@name = $updateForms/@name)]" />-->
            <xsl:apply-templates select="row" />
<!--            <xsl:apply-templates select="$updateForms" />-->
<!--            <xsl:apply-templates select="$updateForms[@name=$name]/row[not(field/dc-schema = $originalForms[@name=$name]/row/field/dc-schema and field/dc-element = $originalForms[@name=$name]/row/field/dc-element and (field/dc-qualifier = $originalForms[@name=$name]/row/field/dc-qualifier or (not(field/dc-qualifier) and not($originalForms[@name=$name]/row/field/dc-qualifier)))) and not(relation-field/relationship-type = $originalForms[@name=$name]/row/relation-field/relationship-type)]" />-->
            <xsl:apply-templates select="$updateForms[@name=$name]/row[not(field/dc-schema = $originalForms[@name=$name]/row/field/dc-schema and field/dc-element = $originalForms[@name=$name]/row/field/dc-element) and not(relation-field/relationship-type = $originalForms[@name=$name]/row/relation-field/relationship-type)]" />
            <!--TODO: qualifiers not working yet-->
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/input-forms/form-definitions/form" mode="new">
        <xsl:variable name="name" select="@name"/>
        <xsl:copy>
            <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
            <xsl:apply-templates select="row" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/input-forms/form-value-pairs">
        <xsl:copy>
            <xsl:apply-templates select="value-pairs" />
            <xsl:apply-templates select="$updateValuePairs[not(@value-pairs-name = $originalValuePairs/@value-pairs-name)]" />
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

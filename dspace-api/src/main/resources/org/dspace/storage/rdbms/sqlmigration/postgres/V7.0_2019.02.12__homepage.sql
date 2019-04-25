
--
-- The contents of this file are subject to the license and copyright
-- detailed in the LICENSE and NOTICE files at the root of the source
-- tree and available online at
--
-- http://www.dspace.org/license/
--

-- ===============================================================
-- WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING
--
-- DO NOT MANUALLY RUN THIS DATABASE MIGRATION. IT WILL BE EXECUTED
-- AUTOMATICALLY (IF NEEDED) BY "FLYWAY" WHEN YOU STARTUP DSPACE.
-- http://flywaydb.org/
-- ===============================================================

-----------------------------------------------------------------
-- This will create the setup for the dspace 7 homepage news api
-----------------------------------------------------------------

CREATE TABLE ui_pages
(
    uuid            uuid NOT NULL  PRIMARY KEY,
    name            varchar(24) NOT NULL,
    title           varchar(256),
    language        varchar(24) NOT NULL,
    dspace_object   uuid REFERENCES dspaceobject(uuid) NOT NULL,
    bitstreamuuid   uuid REFERENCES bitstream(uuid)
);

CREATE INDEX ui_pages_name_idx ON ui_pages(name);
CREATE INDEX ui_pages_language_idx ON ui_pages(language);
CREATE INDEX ui_pages_dspace_object_idx on ui_pages(dspace_object);
ALTER TABLE ui_pages ADD CONSTRAINT uq_ui_pages_name_language UNIQUE(name,language,dspace_object);
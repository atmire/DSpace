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

-------------------------------------------------------------
-- This will create the setup for the dspace 7 entities usage
-------------------------------------------------------------
CREATE TABLE entity_type
(
    uuid                    uuid NOT NULL PRIMARY KEY,
    label                   varchar(32) UNIQUE NOT NULL
);

CREATE TABLE relationship_type
(
    uuid                    uuid NOT NULL PRIMARY KEY,
    left_type               uuid NOT NULL REFERENCES entity_type(uuid),
    right_type              uuid NOT NULL REFERENCES entity_type(uuid),
    left_label              varchar(32) NOT NULL,
    right_label             varchar(32) NOT NULL,
    left_min_cardinality    INTEGER,
    left_max_cardinality    INTEGER,
    right_min_cardinality   INTEGER,
    right_max_cardinality   INTEGER
);

CREATE TABLE relationship
(
    uuid                    uuid NOT NULL PRIMARY KEY,
    left_id                 uuid NOT NULL REFERENCES item(uuid),
    type_id                 uuid NOT NULL REFERENCES relationship_type(uuid),
    right_id                uuid NOT NULL REFERENCES item(uuid),
    place                   INTEGER,
    CONSTRAINT u_constraint UNIQUE (left_id, type_id, right_id)

);
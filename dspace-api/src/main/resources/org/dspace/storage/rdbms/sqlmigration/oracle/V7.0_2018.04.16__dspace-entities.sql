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
    left_type               uuid NOT NULL,
    right_type              uuid NOT NULL,
    left_label              varchar(32) NOT NULL,
    right_label             varchar(32) NOT NULL,
    left_min_cardinality    NUMBER(38),
    left_max_cardinality    NUMBER(38),
    right_min_cardinality   NUMBER(38),
    right_max_cardinality   NUMBER(38),
    FOREIGN KEY (left_type)   REFERENCES entity_type(uuid),
    FOREIGN KEY (right_type)  REFERENCES entity_type(uuid)
);

CREATE TABLE relationship
(
    uuid                    uuid NOT NULL PRIMARY KEY,
    left_id                 uuid NOT NULL REFERENCES item(uuid),
    type_id                 uuid NOT NULL REFERENCES relationship_type(uuid),
    right_id                uuid NOT NULL REFERENCES item(uuid),
    place                   NUMBER(38),
    CONSTRAINT u_constraint UNIQUE (left_id, type_id, right_id)

);
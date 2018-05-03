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
    id                      NUMBER(38) NOT NULL PRIMARY KEY,
    label                   varchar(32) UNIQUE NOT NULL
);

CREATE TABLE relationship_type
(
    id                      NUMBER(38) NOT NULL PRIMARY KEY,
    left_type               NUMBER(38) NOT NULL,
    right_type              NUMBER(38) NOT NULL,
    left_label              varchar(32) NOT NULL,
    right_label             varchar(32) NOT NULL,
    left_min_cardinality    NUMBER(38),
    left_max_cardinality    NUMBER(38),
    right_min_cardinality   NUMBER(38),
    right_max_cardinality   NUMBER(38),
    FOREIGN KEY (left_type)   REFERENCES entity_type(id),
    FOREIGN KEY (right_type)  REFERENCES entity_type(id)
);

CREATE TABLE relationship
(
    id                      NUMBER(38) NOT NULL PRIMARY KEY,
    left_id                 uuid NOT NULL REFERENCES item(uuid),
    type_id                 NUMBER(38) NOT NULL REFERENCES relationship_type(id),
    right_id                uuid NOT NULL REFERENCES item(uuid),
    place                   NUMBER(38),
    CONSTRAINT u_constraint UNIQUE (left_id, type_id, right_id)

);

CREATE INDEX entity_type_label_idx ON entity_type(label);
CREATE INDEX relationship_type_by_types_and_labels_idx ON relationship_type(left_type, right_type, left_label, right_label);
CREATE INDEX relationship_type_by_left_type_idx ON relationship_type(left_type);
CREATE INDEX relationship_type_by_right_type_idx ON relationship_type(right_type);
CREATE INDEX relationship_type_by_left_label_idx ON relationship_type(left_label);
CREATE INDEX relationship_type_by_right_label_idx ON relationship_type(right_label);
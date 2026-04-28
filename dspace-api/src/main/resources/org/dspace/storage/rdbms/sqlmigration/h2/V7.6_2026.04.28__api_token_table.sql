--
-- The contents of this file are subject to the license and copyright
-- detailed in the LICENSE and NOTICE files at the root of the source
-- tree and available online at
--
-- http://www.dspace.org/license/
--

-----------------------------------------------------------------------------------
-- Create table for API tokens
-----------------------------------------------------------------------------------

CREATE TABLE api_token
(
    hash                VARCHAR(128) NOT NULL,
    digest_algorithm    VARCHAR(16),
    salt                VARCHAR(32),
    eperson_id          UUID NOT NULL,
    created             TIMESTAMP,
    expiry              TIMESTAMP,
    CONSTRAINT api_token_pkey PRIMARY KEY (hash),
    CONSTRAINT api_token_eperson_id_fkey FOREIGN KEY (eperson_id) REFERENCES eperson (uuid)
);

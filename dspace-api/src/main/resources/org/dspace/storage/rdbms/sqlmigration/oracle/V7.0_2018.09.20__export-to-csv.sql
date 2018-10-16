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
-- This will create the setup for the dspace 7 export to csv
-------------------------------------------------------------
CREATE SEQUENCE export_csv_file_id_seq;
CREATE TABLE export_csv_file
(
    dso                     RAW(16) NOT NULL REFERENCES dspaceobject(uuid),
    date                    TIMESTAMP NOT NULL,
    bitstream_id            RAW(16),
    status                  varchar(32),
    unique(dso, date)
);
CREATE INDEX export_csv_file_dso_idx ON export_csv_file(dso);
CREATE INDEX export_csv_file_all_idx ON export_csv_file(dso, id, date, bitstream_id, status);
CREATE INDEX export_csv_file_bitstream_id_idx ON export_csv_file(bitstream_id);
CREATE INDEX export_csv_file_status_idx ON export_csv_file(status);
CREATE INDEX export_csv_file_date_idx ON export_csv_file(date);
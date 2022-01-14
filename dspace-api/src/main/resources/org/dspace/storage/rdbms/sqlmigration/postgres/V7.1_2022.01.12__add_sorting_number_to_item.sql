--
-- The contents of this file are subject to the license and copyright
-- detailed in the LICENSE and NOTICE files at the root of the source
-- tree and available online at
--
-- http://www.dspace.org/license/
--

create sequence item_sorting_number_seq;
alter table item add sorting_number integer default nextval('item_sorting_number_seq');
alter sequence item_sorting_number_seq owned by item.sorting_number

package org.dspace.content.dao;

import org.dspace.content.dao.pojo.Location;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface LocationDAO {

    Location findLocationByDsoId(Context context, UUID dsoId) throws SQLException;

    List<Location> findAllLocations(Context context) throws SQLException;
}

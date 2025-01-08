package org.dspace.content.dao.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Location {
    private UUID dsoId;
    private List<UUID> locationComm;
    private List<UUID> locationColl;

    public Location(Object[] obj) {
        this.dsoId = UUID.fromString((String) obj[0]);
        this.locationComm = convertStringToUUIDList((String) obj[1]);
        this.locationColl = convertStringToUUIDList((String) obj[2]);
    }

    public Location() {
    }

    public UUID getDsoId() {
        return dsoId;
    }

    public void setDsoId(UUID dsoId) {
        this.dsoId = dsoId;
    }

    public List<UUID> getLocationComm() {
        return locationComm;
    }

    public void setLocationComm(List<UUID> locationComm) {
        this.locationComm = locationComm;
    }

    public List<UUID> getLocationColl() {
        return locationColl;
    }

    public void setLocationColl(List<UUID> locationColl) {
        this.locationColl = locationColl;
    }

    private List<UUID> convertStringToUUIDList(String arrayString) {
        if (arrayString == null || arrayString.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(arrayString.split(","))
                     .map(UUID::fromString)
                     .collect(Collectors.toList());
    }
}

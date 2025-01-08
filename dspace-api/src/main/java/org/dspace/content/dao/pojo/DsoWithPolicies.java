package org.dspace.content.dao.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class DsoWithPolicies {

    private UUID dsoId;
    // Policies already prefixed with correct 'g' or 'e' for group or eperson respectively
    private Set<String> readPolicyIds;
    private Set<String> editPolicyIds;
    private Set<String> adminPolicyIds;

    public DsoWithPolicies(Object[] object) {
        this.dsoId = UUID.fromString((String) object[0]);
        this.readPolicyIds = convertStringToList((String) object[1]);
        this.editPolicyIds = convertStringToList((String) object[2]);
        this.adminPolicyIds = convertStringToList((String) object[3]);
    }

    public UUID getDsoId() {
        return dsoId;
    }

    public void setDsoId(UUID dsoId) {
        this.dsoId = dsoId;
    }

    public Set<String> getReadPolicyIds() {
        return readPolicyIds;
    }

    public void setReadPolicyIds(Set<String> readPolicyIds) {
        this.readPolicyIds = readPolicyIds;
    }

    public Set<String> getEditPolicyIds() {
        return editPolicyIds;
    }

    public void setEditPolicyIds(Set<String> editPolicyIds) {
        this.editPolicyIds = editPolicyIds;
    }

    public Set<String> getAdminPolicyIds() {
        return adminPolicyIds;
    }

    public void setAdminPolicyIds(Set<String> adminPolicyIds) {
        this.adminPolicyIds = adminPolicyIds;
    }

    private Set<String> convertStringToList(String arrayString) {
        if (arrayString == null || arrayString.isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(arrayString.split(","))
                        .collect(Collectors.toSet());
    }
}

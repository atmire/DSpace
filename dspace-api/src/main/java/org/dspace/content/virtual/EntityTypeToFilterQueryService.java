package org.dspace.content.virtual;

import java.util.Map;

public class EntityTypeToFilterQueryService {

    private Map<String, String> map;

    public void setMap(Map map) {
        this.map = map;
    }

    public Map getMap() {
        return map;
    }

    public String getFilterQueryForKey(String key) {
        return map.get(key);
    }

    public boolean hasKey(String key) {
        return map.containsKey(key);
    }
}

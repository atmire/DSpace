package org.dspace.content;

import java.util.List;

public class Entity {

    private Item item;
    private List<Relationship> relationships;

    public Entity(Item item,List<Relationship> relationshipList) {
        setItem(item);
        setRelationships(relationshipList);
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }
}

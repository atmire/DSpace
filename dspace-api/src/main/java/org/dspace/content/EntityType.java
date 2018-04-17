package org.dspace.content;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "entity_type")
public class EntityType {

    @Id
    @Column(name = "uuid", unique = true, nullable = false, insertable = true, updatable = false)
    protected java.util.UUID id;

    @Column(name = "label", nullable = false)
    private String label;

    public UUID getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    protected EntityType() {

    }
}

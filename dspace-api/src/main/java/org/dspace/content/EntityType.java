package org.dspace.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "entity_type")
public class EntityType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_type_id_seq")
    @SequenceGenerator(name = "entity_type_id_seq", sequenceName = "entity_type_id_seq", allocationSize = 1)
    @Column(name = "id", unique = true, nullable = false, insertable = true, updatable = false)
    protected Integer id;

    @Column(name = "label", nullable = false)
    private String label;

    public Integer getId() {
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

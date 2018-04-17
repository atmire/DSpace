package org.dspace.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "relationship")
public class Relationship {

    @Id
    @Column(name = "uuid", unique = true, nullable = false, insertable = true, updatable = false)
    protected java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "left_id", nullable = false)
    private EntityType leftEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private RelationshipType relationshipType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "right_id", nullable = false)
    private EntityType rightEntity;

    @Column(name = "place")
    private int place;

}

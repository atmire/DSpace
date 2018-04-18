package org.dspace.content;

import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "relationship_type")
public class RelationshipType {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "uuid", unique = true, nullable = false, insertable = true, updatable = false)
    protected java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "left_type", nullable = false)
    private EntityType leftType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "right_type", nullable = false)
    private EntityType rightType;

    @Column(name = "left_label", nullable = false)
    private String leftLabel;

    @Column(name = "right_label", nullable = false)
    private String rightLabel;

    @Column(name = "left_min_cardinality")
    private int leftMinCardinality;

    @Column(name = "left_max_cardinality")
    private int leftMaxCardinality;

    @Column(name = "right_min_cardinality")
    private int rightMinCardinality;

    @Column(name = "right_max_cardinality")
    private int rightMaxCardinality;


    public UUID getId() {
        return id;
    }

    public EntityType getLeftType() {
        return leftType;
    }

    public void setLeftType(EntityType leftType) {
        this.leftType = leftType;
    }

    public EntityType getRightType() {
        return rightType;
    }

    public void setRightType(EntityType rightType) {
        this.rightType = rightType;
    }

    public String getLeftLabel() {
        return leftLabel;
    }

    public void setLeftLabel(String leftLabel) {
        this.leftLabel = leftLabel;
    }

    public String getRightLabel() {
        return rightLabel;
    }

    public void setRightLabel(String rightLabel) {
        this.rightLabel = rightLabel;
    }

    public int getLeftMinCardinality() {
        return leftMinCardinality;
    }

    public void setLeftMinCardinality(int leftMinCardinality) {
        this.leftMinCardinality = leftMinCardinality;
    }

    public int getLeftMaxCardinality() {
        return leftMaxCardinality;
    }

    public void setLeftMaxCardinality(int leftMaxCardinality) {
        this.leftMaxCardinality = leftMaxCardinality;
    }

    public int getRightMinCardinality() {
        return rightMinCardinality;
    }

    public void setRightMinCardinality(int rightMinCardinality) {
        this.rightMinCardinality = rightMinCardinality;
    }

    public int getRightMaxCardinality() {
        return rightMaxCardinality;
    }

    public void setRightMaxCardinality(int rightMaxCardinality) {
        this.rightMaxCardinality = rightMaxCardinality;
    }
}

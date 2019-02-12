/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.pages;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.dspace.content.Bitstream;
import org.dspace.core.ReloadableEntity;
import org.hibernate.annotations.GenericGenerator;

/**
 * This class represents the ui_pages table in the Database as an Entity.
 */
@Entity
@Table(name = "ui_pages")
public class Page implements ReloadableEntity<UUID> {


    /**
     *  This is the UUID for the Page class. It's automatically generated and stored in the uuid column.
     *  This cannot be null and has to be unique
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "uuid", unique = true, nullable = false)
    private UUID id;

    /**
     * This is the name property of the Page class. It is stored in the name column and it cannot be null
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * This is the title property of the Page class. It is stored in the title column
     */
    @Column(name = "title")
    private String title;

    /**
     * This is the language property of the Page class. It is stored in the language column and it cannot be null
     */
    @Column(name = "language", nullable = false)
    private String language;

    /**
     * This is the bitstream property of the Page class. It is stored as it's UUID in the bitstreamuuid column.
     * This cannot be null and represents a one to one relationship between the Bitstream and the Page objects
     */
    @OneToOne
    @JoinColumn(name = "bitstreamuuid", nullable = false)
    private Bitstream bitstream;

    /**
     * Generic setter for the id
     * @param id    The id that this id field should be set to
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Generic getter for the id
     * @return  the id of this Page object
     */
    public UUID getID() {
        return id;
    }

    /**
     * Generic getter for the name
     * @return  the name of this Page object
     */
    public String getName() {
        return name;
    }

    /**
     * Generic setter for the name
     * @param name  the name that this name field should be set to
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Generic getter for the title
     * @return  the title of this Page object
     */
    public String getTitle() {
        return title;
    }

    /**
     * Generic setter for the title
     * @param title the title that this title field should be set to
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Generic getter for the language
     * @return  the language of this Page object
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Generic setter for the language
     * @param language  the language that this language field should be set to
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Generic getter for the bitstream
     * @return  the bitstream of this Page object
     */
    public Bitstream getBitstream() {
        return bitstream;
    }

    /**
     * Generic setter for the bitstream
     * @param bitstream the bitstream that this bitstream field should be set to
     */
    public void setBitstream(Bitstream bitstream) {
        this.bitstream = bitstream;
    }
}

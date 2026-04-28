/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.api.token;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.dspace.eperson.EPerson;

/**
 * Class representing an API token in DSpace.
 *
 * @author Bram Maegerman (bram.maegerman at atmire.com)
 */
@Entity
@Table(name = "api_token")
public class ApiToken {

    /**
     * This is only used when creating new tokens
     * and will be empty when retrieving a token from the database
     */
    @Transient
    private volatile String token;

    @Id
    @Column(name = "hash", length = 128)
    private String hash;

    @Column(name = "digest_algorithm", length = 16)
    private String digestAlgorithm;

    @Column(name = "salt", length = 32)
    private String salt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson_id", nullable = false)
    private EPerson ePerson;

    @Column(name = "created", length = 32)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "expiry", length = 32)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiry;

    public ApiToken(String token) {
        this.token = token;
    }

    public ApiToken() {}

    public String getToken() {
        return token;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public EPerson getEPerson() {
        return ePerson;
    }

    public void setEPerson(EPerson eperson) {
        this.ePerson = eperson;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getExpiry() {
        return expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }
}

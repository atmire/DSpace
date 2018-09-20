package org.dspace.content;

import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.dspace.core.ReloadableEntity;

@Entity
@Table(name = "export_csv_file")
public class ExportToCsv implements ReloadableEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "export_zip_file_id_seq")
    @SequenceGenerator(name = "export_zip_file_id_seq", sequenceName = "export_zip_file_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dso", nullable = false)
    private DSpaceObject dso;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "bitstream_id")
    private UUID bitstreamId;

    @Column(name = "status")
    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DSpaceObject getDso() {
        return dso;
    }

    public void setDso(DSpaceObject dso) {
        this.dso = dso;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public UUID getBitstreamId() {
        return bitstreamId;
    }

    public void setBitstreamId(UUID bitstreamId) {
        this.bitstreamId = bitstreamId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getID() {
        return id;
    }
}
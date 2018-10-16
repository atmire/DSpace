package org.dspace.content;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.dspace.export.ExportStatus;

@Entity
@Table(name = "export_csv_file")
public class ExportToCsv implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dso", nullable = false)
    @Id
    private DSpaceObject dso;

    @Column(name = "date")
    @Id
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "bitstream_id")
    private UUID bitstreamId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ExportStatus status;

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

    public ExportStatus getStatus() {
        return status;
    }

    public void setStatus(ExportStatus status) {
        this.status = status;
    }

}
package org.dspace.export;

public enum ExportStatus {
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private String value;

    public String getValue() {
        return this.value;
    }

    private ExportStatus(String valueString) {
        this.value = valueString;
    }
}

package me.mmtr.xwarehouse.model.enums;

public enum WarehouseOrderStatus {
    WAITING("WAITING"),
    IN_PROGRESS("IN PROGRESS"),
    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED");


    private final String label;

    WarehouseOrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

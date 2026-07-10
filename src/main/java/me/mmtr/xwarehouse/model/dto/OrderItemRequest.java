package me.mmtr.xwarehouse.model.dto;

public class OrderItemRequest {
    private Long warehouseItemId;
    private Long quantity;

    public OrderItemRequest() {
    }

    public OrderItemRequest(Long warehouseItemId, Long quantity) {
        this.warehouseItemId = warehouseItemId;
        this.quantity = quantity;
    }

    public Long getWarehouseItemId() {
        return warehouseItemId;
    }

    public void setWarehouseItemId(Long warehouseItemId) {
        this.warehouseItemId = warehouseItemId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}

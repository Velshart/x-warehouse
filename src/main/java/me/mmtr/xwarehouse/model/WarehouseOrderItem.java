package me.mmtr.xwarehouse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouse_order_items")
public class WarehouseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private WarehouseOrder warehouseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private WarehouseItem warehouseItem;

    @Column(nullable = false)
    private Long orderedQuantity;

    public WarehouseOrderItem() {}

    public WarehouseOrderItem(
            WarehouseOrder warehouseOrder,
            WarehouseItem warehouseItem,
            Long orderedQuantity
    ) {
        this.warehouseOrder = warehouseOrder;
        this.warehouseItem = warehouseItem;
        this.orderedQuantity = orderedQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WarehouseOrder getWarehouseOrder() {
        return warehouseOrder;
    }

    public void setWarehouseOrder(WarehouseOrder warehouseOrder) {
        this.warehouseOrder = warehouseOrder;
    }

    public WarehouseItem getWarehouseItem() {
        return warehouseItem;
    }

    public void setWarehouseItem(WarehouseItem warehouseItem) {
        this.warehouseItem = warehouseItem;
    }

    public Long getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(Long orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }
}


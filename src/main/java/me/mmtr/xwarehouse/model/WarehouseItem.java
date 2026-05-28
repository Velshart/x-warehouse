package me.mmtr.xwarehouse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouse_items")
public class WarehouseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String sku;

    private Long quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private WarehouseZone warehouseZone;

    public WarehouseItem() {
    }

    public WarehouseItem(
            String name,
            String sku,
            Long quantity,
            WarehouseZone warehouseZone
    ) {
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
        this.warehouseZone = warehouseZone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public WarehouseZone getWarehouseZone() {
        return warehouseZone;
    }

    public void setWarehouseZone(WarehouseZone warehouseZone) {
        this.warehouseZone = warehouseZone;
    }
}

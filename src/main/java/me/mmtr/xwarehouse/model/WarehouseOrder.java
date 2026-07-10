package me.mmtr.xwarehouse.model;

import jakarta.persistence.*;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "warehouse_orders")
public class WarehouseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id", nullable = false)
    private WarehouseContractor warehouseContractor;

    @OneToMany(
            mappedBy = "warehouseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<WarehouseOrderItem> warehouseOrderItems = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    WarehouseOrderStatus warehouseOrderStatus;

    public WarehouseOrder() {}


    public WarehouseOrder(
            Warehouse warehouse,
            WarehouseContractor warehouseContractor,
            List<WarehouseOrderItem> warehouseOrderItems,
            WarehouseOrderStatus warehouseOrderStatus
    ) {
        this.warehouse = warehouse;
        this.warehouseContractor = warehouseContractor;
        this.warehouseOrderItems = warehouseOrderItems;
        this.warehouseOrderStatus = warehouseOrderStatus;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public WarehouseContractor getWarehouseContractor() {
        return warehouseContractor;
    }

    public void setWarehouseContractor(WarehouseContractor warehouseContractor) {
        this.warehouseContractor = warehouseContractor;
    }

    public List<WarehouseOrderItem> getWarehouseOrderItems() {
        return warehouseOrderItems;
    }

    public void setWarehouseOrderItems(List<WarehouseOrderItem> warehouseOrderItems) {
        this.warehouseOrderItems = warehouseOrderItems;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public WarehouseOrderStatus getWarehouseOrderStatus() {
        return warehouseOrderStatus;
    }

    public void setWarehouseOrderStatus(WarehouseOrderStatus warehouseOrderStatus) {
        this.warehouseOrderStatus = warehouseOrderStatus;
    }
}

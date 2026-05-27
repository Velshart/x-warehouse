package me.mmtr.xwarehouse.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "warehouse_zones")
public class WarehouseZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 100)
    @Size(max = 100, message = "Zone description cannot exceed 100 characters")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @OneToMany(
            mappedBy = "warehouseZone",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<WarehouseItem> warehouseItems = new ArrayList<>();

    public WarehouseZone() {
    }

    public WarehouseZone(String name, Warehouse warehouse) {
        this.name = name;
        this.warehouse = warehouse;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public List<WarehouseItem> getWarehouseItems() {
        return warehouseItems;
    }

    public void setWarehouseItems(List<WarehouseItem> warehouseItems) {
        this.warehouseItems = warehouseItems;
    }
}

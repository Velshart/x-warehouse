package me.mmtr.xwarehouse.model;

import jakarta.persistence.*;
import me.mmtr.xwarehouse.model.enums.WarehouseRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_members")
public class WarehouseMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private WarehouseRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    public WarehouseMember() {
    }

    public WarehouseMember(Warehouse warehouse, User user, WarehouseRole role) {
        this.warehouse = warehouse;
        this.user = user;
        this.role = role;
    }

    @PrePersist
    public void prePersist() {
        this.assignedAt = LocalDateTime.now();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WarehouseRole getRole() {
        return role;
    }

    public void setRole(WarehouseRole role) {
        this.role = role;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}

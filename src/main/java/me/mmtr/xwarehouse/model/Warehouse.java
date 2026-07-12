package me.mmtr.xwarehouse.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "warehouses")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    @Size(
            min = 1,
            max = 30,
            message = "Warehouse name must be between 1 and 30 characters long"
    )
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(
            mappedBy = "warehouse",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<WarehouseMember> members = new ArrayList<>();

    @OneToMany(
            mappedBy = "warehouse",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<WarehouseZone> zones = new ArrayList<>();

    @OneToMany(
            mappedBy = "warehouse",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<WarehouseContractor> contractors = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Warehouse() {
    }

    public Warehouse(String name, User owner) {
        this.name = name;
        this.owner = owner;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<WarehouseMember> getMembers() {
        return members;
    }

    public void setMembers(List<WarehouseMember> members) {
        this.members = members;
    }

    public List<WarehouseZone> getZones() {
        return zones;
    }

    public void setZones(List<WarehouseZone> zones) {
        this.zones = zones;
    }

    public List<WarehouseContractor> getContractors() {
        return contractors;
    }

    public void setContractors(List<WarehouseContractor> contractors) {
        this.contractors = contractors;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

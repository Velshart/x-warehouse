package me.mmtr.xwarehouse.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Size(
            min = 1,
            max = 20,
            message = "Username must be between 1 and 20 characters long"
    )
    private String username;

    @Column(nullable = false)
    @Size(
            min = 1,
            max = 100,
            message = "Password must be between 1 and 100 characters long"
    )
    private String password;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    List<Warehouse> ownedWarehouses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<WarehouseMember> warehouseAssignments = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Warehouse> getOwnedWarehouses() {
        return ownedWarehouses;
    }

    public void setOwnedWarehouses(List<Warehouse> ownedWarehouses) {
        this.ownedWarehouses = ownedWarehouses;
    }

    public List<WarehouseMember> getWarehouseAssignments() {
        return warehouseAssignments;
    }

    public void setWarehouseAssignments(List<WarehouseMember> warehouseAssignments) {
        this.warehouseAssignments = warehouseAssignments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

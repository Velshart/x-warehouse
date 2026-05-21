package me.mmtr.xwarehouse.service;

import jakarta.transaction.Transactional;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseMember;
import me.mmtr.xwarehouse.model.enums.WarehouseRole;
import me.mmtr.xwarehouse.repository.UserRepository;
import me.mmtr.xwarehouse.repository.WarehouseMemberRepository;
import me.mmtr.xwarehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final WarehouseMemberRepository warehouseMemberRepository;

    public WarehouseService(
            WarehouseRepository warehouseRepository,
            UserRepository userRepository,
            WarehouseMemberRepository warehouseMemberRepository
    ) {
        this.warehouseRepository = warehouseRepository;
        this.userRepository = userRepository;
        this.warehouseMemberRepository = warehouseMemberRepository;
    }

    @Transactional
    public Warehouse createWarehouse(String name, User owner) {
        Warehouse warehouse = new Warehouse(name, owner);

        return warehouseRepository.save(warehouse);
    }

    public List<Warehouse> getAvailableWarehouses(Long userId) {
        return warehouseRepository.findAllAccessibleByUserId(userId);
    }

    @Transactional
    public void addMember(
            Long warehouseId,
            String username,
            WarehouseRole role,
            User currentUser
    ) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Warehouse with id " + warehouseId + " does not exist")
                );

        if (!warehouse.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException(
                    "Current user is not the owner of this warehouse"
            );
        }

        User userToAddToWarehouse = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Username " + username + " does not exist"
                ));

        if (warehouse.getOwner().getId().equals(userToAddToWarehouse.getId())) {
            throw new IllegalArgumentException(
                    "User " + username + " is already an owner of this warehouse"
            );
        }

        warehouseMemberRepository
                .findByWarehouseIdAndUserId(warehouseId, userToAddToWarehouse.getId())
                .ifPresent(m -> {
                    throw new IllegalArgumentException(
                            "User " + username + " is already a member of this warehouse");
                });

        WarehouseMember warehouseMember =
                new WarehouseMember(warehouse, userToAddToWarehouse, role);

        warehouseMemberRepository.save(warehouseMember);
    }

    @Transactional
    public void deleteWarehouse(Long warehouseId, User currentUser) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Warehouse with id " + warehouseId + " does not exist"
                        )
                );

        if (!warehouse.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalStateException(
                    "Current user is not the owner of this warehouse"
            );
        }

        warehouseRepository.delete(warehouse);
    }

    public boolean hasAccess(
            Long warehouseId,
            Long userId,
            boolean requireWriteAccess
    ) {
        Warehouse warehouse = warehouseRepository
                .findById(warehouseId).orElse(null);

        if (warehouse == null) {
            return false;
        }

        if (warehouse.getOwner().getId().equals(userId)) {
            return true;
        }

        return warehouseMemberRepository
                .findByWarehouseIdAndUserId(warehouseId, userId)
                .map(warehouseMember -> {
                    if (requireWriteAccess) {
                        return warehouseMember.getRole() == WarehouseRole.ADMIN;
                    }

                    return true;
                }).orElse(false);
    }
}

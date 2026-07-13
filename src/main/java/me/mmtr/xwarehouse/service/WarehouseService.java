package me.mmtr.xwarehouse.service;

import jakarta.transaction.Transactional;
import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
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

    public Warehouse getWarehouseByIdAndUser(Long warehouseId, User currentUser) {
        validateReadAccess(warehouseId, currentUser);

        return warehouseRepository.findWarehouseByIdAndUserId(
                warehouseId,
                currentUser.getId()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Warehouse not found"
        ));
    }

    @Transactional
    public Warehouse createWarehouse(String name, User owner) {
        String warehouseName = name.trim();
        if (warehouseName.isEmpty()) {
            throw new BusinessValidationException("Name cannot be empty");
        }
        Warehouse warehouse = new Warehouse(name, owner);

        return warehouseRepository.save(warehouse);
    }

    public List<Warehouse> getAvailableWarehouses(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return warehouseRepository.findAllAccessibleByUserId(userId);
        }

        return warehouseRepository.findAllAccessibleByUserIdAndNameContaining(
                userId, keyword
        );
    }

    @Transactional
    public void addMember(
            Long warehouseId,
            String username,
            WarehouseRole role,
            User currentUser
    ) {
        Warehouse warehouse = getWarehouseByIdAndUser(warehouseId, currentUser);

        if (!warehouse.getOwner().getId().equals(currentUser.getId())) {
            throw new ResourceAccessException(
                    "This user is not the owner of this warehouse"
            );
        }

        User userToAddToWarehouse = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"
                ));

        if (warehouse.getOwner().getId().equals(userToAddToWarehouse.getId())) {
            throw new BusinessValidationException(
                    "This user is already an owner of this warehouse"
            );
        }

        warehouseMemberRepository
                .findByWarehouseIdAndUserId(warehouseId, userToAddToWarehouse.getId())
                .ifPresent(m -> {
                    throw new BusinessValidationException(
                            "This user is already a member " +
                                    "of this warehouse"
                    );
                });

        WarehouseMember warehouseMember =
                new WarehouseMember(warehouse, userToAddToWarehouse, role);

        warehouseMemberRepository.save(warehouseMember);
    }

    @Transactional
    public void removeMember(
            Long warehouseId,
            Long userId,
            User currentUser
    ) {
        validateReadAccess(warehouseId, currentUser);

        Warehouse warehouse = getWarehouseByIdAndUser(warehouseId, currentUser);

        if (userId.equals(warehouse.getOwner().getId())) {
            throw new BusinessValidationException(
                    "An owner cannot be removed from members list."
            );
        }

        WarehouseMember memberToRemove =
                warehouseMemberRepository
                        .findByWarehouseIdAndUserId(warehouseId, userId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "This user is not a member of this warehouse"
                        ));

        boolean isCurrentUserAnOwner = warehouse
                .getOwner().getId().equals(currentUser.getId());

        boolean isSelfRemoval = currentUser.getId().equals(userId);
        boolean isCurrentUserAnAdmin = !isCurrentUserAnOwner &&
                hasAccess(warehouseId, currentUser.getId(), true);

        if (!isCurrentUserAnOwner && !isSelfRemoval) {
            if (isCurrentUserAnAdmin) {
                if (memberToRemove.getRole().equals(WarehouseRole.ADMIN)) {
                    throw new ResourceAccessException(
                            "Current user is not permitted to perform this operation"
                    );
                }
            } else {
                throw new ResourceAccessException(
                        "Current user is not permitted to perform this operation"
                );
            }
        }

        warehouseMemberRepository.delete(memberToRemove);
    }

    @Transactional
    public void updateWarehouse(Long warehouseId, String newName, User currentUser) {
        Warehouse warehouse = getWarehouseByIdAndUser(warehouseId, currentUser);

        if (!warehouse.getOwner().getId().equals(currentUser.getId())) {
            throw new ResourceAccessException(
                    "Current user is not the owner of this warehouse"
            );
        }

        warehouse.setName(newName);
        warehouseRepository.save(warehouse);
    }

    @Transactional
    public void deleteWarehouse(Long warehouseId, User currentUser) {
        Warehouse warehouse = getWarehouseByIdAndUser(warehouseId, currentUser);

        if (!warehouse.getOwner().getId().equals(currentUser.getId())) {
            throw new ResourceAccessException(
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

    public void validateReadAccess(Long warehouseId, User currentUser) {
        if (!hasAccess(warehouseId, currentUser.getId(), false)) {
            throw new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            );
        }
    }

    public void validateWriteAccess(Long warehouseId, User currentUser) {
        if (!hasAccess(warehouseId, currentUser.getId(), true)) {
            throw new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            );
        }
    }
}

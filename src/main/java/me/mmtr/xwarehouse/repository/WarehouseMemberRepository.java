package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.WarehouseMember;
import me.mmtr.xwarehouse.model.enums.WarehouseRole;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseMemberRepository extends
        JpaRepository<@NonNull WarehouseMember, @NonNull Integer> {

    Optional<WarehouseMember> findByWarehouseIdAndUserId(
            @NonNull Long warehouseId,
            @NonNull Long userId
    );

    boolean existsByWarehouseIdAndUserIdAndRole(
            @NonNull Long warehouseId,
            @NonNull Long userId,
            @NonNull WarehouseRole role
    );
}

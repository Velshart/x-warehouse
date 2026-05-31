package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.WarehouseZone;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseZoneRepository extends JpaRepository<@NonNull WarehouseZone, @NonNull Long> {
    Optional<WarehouseZone> findByWarehouseIdAndName(Long warehouseId, String name);

    List<WarehouseZone> findAllByWarehouseId(Long warehouseId);

    @Query(
            "SELECT z FROM WarehouseZone z " +
                    "WHERE z.warehouse.id = :warehouseId " +
                    "AND LOWER(z.name) LIKE LOWER(CONCAT('%', :keyword, '%'))"
    )
    List<WarehouseZone> findAllByWarehouseIdAndNameContaining(
            @Param("warehouseId") Long warehouseId,
            @Param("keyword") String keyword
    );
}

package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.WarehouseItem;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseItemRepository extends JpaRepository<@NonNull WarehouseItem, @NonNull Long> {
    Optional<WarehouseItem> findByIdAndWarehouseZoneWarehouseId(Long id, Long warehouseId);

    Optional<WarehouseItem> findByWarehouseZoneWarehouseIdAndSku(
            Long warehouseId, String sku
    );

    List<WarehouseItem> findAllByWarehouseZoneWarehouseId(Long warehouseId);

    List<WarehouseItem> findAllByWarehouseZoneId(Long zoneId);

    @Query("SELECT i FROM WarehouseItem i WHERE i.warehouseZone.id = :zoneId " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<WarehouseItem> findAllByZoneIdAndNameOrSkuContaining(
            @Param("zoneId") Long zoneId,
            @Param("keyword") String keyword
    );
}

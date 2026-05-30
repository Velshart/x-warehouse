package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.WarehouseZone;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseZoneRepository extends JpaRepository<@NonNull WarehouseZone, @NonNull Long> {
    Optional<WarehouseZone> findByWarehouseIdAndName(Long warehouseId, String name);
    List<WarehouseZone> findAllByWarehouseId(Long warehouseId);
}

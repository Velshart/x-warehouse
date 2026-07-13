package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.WarehouseOrder;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseOrderRepository extends JpaRepository<@NonNull WarehouseOrder, @NonNull Long> {
    List<WarehouseOrder> findAllByWarehouseIdAndWarehouseContractorIdAndWarehouseOrderType(Long warehouseId, Long warehouseContractorId, WarehouseOrderType warehouseOrderType);

    Optional<WarehouseOrder> findByIdAndWarehouseId(@NonNull Long orderId, @NonNull Long warehouseId);
}

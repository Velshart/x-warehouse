package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.WarehouseOrder;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseOrderRepository extends JpaRepository<@NonNull WarehouseOrder, @NonNull Long> {
    List<WarehouseOrder> findAllByWarehouseIdAndWarehouseContractorId(@NonNull Long warehouseId, @NonNull Long contractorId);

    Optional<WarehouseOrder> findByIdAndWarehouseId(@NonNull Long orderId, @NonNull Long warehouseId);

    List<WarehouseOrder> findAllByWarehouseId(@NonNull Long id);

    List<WarehouseOrder> findAllByWarehouseIdAndWarehouseContractorNameContaining(
            Long warehouseId,
            String keyword
    );
}

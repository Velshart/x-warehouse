package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.WarehouseContractor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseContractorRepository extends JpaRepository<@NonNull WarehouseContractor, @NonNull Long> {

    List<WarehouseContractor> findAllByWarehouseId(Long warehouseId);

    Optional<WarehouseContractor> findByIdAndWarehouseId(Long id, Long warehouseId);

    Optional<WarehouseContractor> findByWarehouseIdAndNip(Long warehouseId, String nip);
}

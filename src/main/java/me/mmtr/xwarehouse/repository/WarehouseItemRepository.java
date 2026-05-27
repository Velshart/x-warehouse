package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.WarehouseItem;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseItemRepository extends JpaRepository<@NonNull WarehouseItem, @NonNull Long> {
    Optional<WarehouseItem> findBySku(String sku);
}

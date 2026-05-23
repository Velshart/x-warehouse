package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.Warehouse;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends
        JpaRepository<@NonNull Warehouse, @NonNull Long> {

    @Query(
            "SELECT DISTINCT w FROM Warehouse w" +
                    " LEFT JOIN w.members m" +
                    " WHERE w.owner.id = :userId" +
                    " OR m.user.id = :userId"
    )
    List<Warehouse> findAllAccessibleByUserId(@Param("userId") Long userId);


    @Query(
            "SELECT w FROM Warehouse w " +
                    "LEFT JOIN WarehouseMember wm ON w.id = wm.warehouse.id" +
                    " WHERE w.id = :warehouseId" +
                    " AND (w.owner.id = :userId OR wm.user.id = :userId)"
    )
    Optional<Warehouse> findWarehouseByIdAndUserId(
            @Param("warehouseId") Long warehouseId,
            @Param("userId") Long userId
    );
}

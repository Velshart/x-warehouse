package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.Warehouse;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}

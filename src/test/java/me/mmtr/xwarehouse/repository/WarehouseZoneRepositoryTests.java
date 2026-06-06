package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class WarehouseZoneRepositoryTests {

    @Autowired
    private WarehouseZoneRepository warehouseZoneRepository;

    @Autowired
    private TestEntityManager entityManager;

    private WarehouseZone warehouseZone;
    private Warehouse warehouse;

    @BeforeEach
    public void setup() {
        User owner = new User("owner", "password");
        entityManager.persist(owner);

        warehouse = new Warehouse("Test Warehouse", owner);
        entityManager.persist(warehouse);

        warehouseZone = new WarehouseZone("Test Zone", warehouse);
        entityManager.persist(warehouseZone);
    }

    @Test
    public void verifyIfZonesAreReturnedCorrectlyByWarehouseIdAndNameContainingKeyword() {
        List<WarehouseZone> warehouseZones =
                warehouseZoneRepository.findAllByWarehouseIdAndNameContaining(
                        warehouse.getId(), "Test"
                );

        assertEquals(1, warehouseZones.size());
        assertEquals(
                warehouseZone.getName(),
                warehouseZones.getFirst().getName()
        );
    }


    @Test
    public void verifyIfNoZonesAreReturnedWhenNoZoneNameContainsKeyword() {
        List<WarehouseZone> warehouseZones =
                warehouseZoneRepository.findAllByWarehouseIdAndNameContaining(
                        warehouse.getId(), "RANDOM KEYWORD"
                );

        assertEquals(0, warehouseZones.size());
    }
}

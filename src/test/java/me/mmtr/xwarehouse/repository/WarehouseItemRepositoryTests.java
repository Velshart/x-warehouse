package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseItem;
import me.mmtr.xwarehouse.model.WarehouseZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class WarehouseItemRepositoryTests {

    @Autowired
    private WarehouseItemRepository warehouseItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private WarehouseZone warehouseZone;

    private WarehouseItem warehouseItem;

    @BeforeEach
    public void setup() {
        User owner = new User("owner", "owner");
        entityManager.persist(owner);

        Warehouse warehouse = new Warehouse("Test Warehouse", owner);
        entityManager.persist(warehouse);

        warehouseZone = new WarehouseZone("Test Zone", warehouse);
        entityManager.persist(warehouseZone);

        warehouseItem = new WarehouseItem(
                "Test Item",
                "TEST-01",
                5L,
                warehouseZone
        );
        entityManager.persist(warehouseItem);

        entityManager.flush();
    }

    @Test
    public void verifyIfAllItemsAreReturnedCorrectlyByNameContainingKeyword() {
        List<WarehouseItem> warehouseItems =
                warehouseItemRepository.findAllByZoneIdAndNameOrSkuContaining(
                        warehouseZone.getId(), "Test"
                );

        assertEquals(1, warehouseItems.size());
        assertEquals(warehouseItem.getName(), warehouseItems.getFirst().getName());
    }

    @Test
    public void verifyIfAllItemsAreReturnedCorrectlyBySkuContainingKeyword() {
        List<WarehouseItem> warehouseItems =
                warehouseItemRepository.findAllByZoneIdAndNameOrSkuContaining(
                        warehouseZone.getId(), "01"
                );

        assertEquals(1, warehouseItems.size());
        assertEquals(warehouseItem.getName(), warehouseItems.getFirst().getName());
    }

    @Test
    public void verifyIfNoItemsAreReturnedWhenNoItemNameContainsKeyword() {
        List<WarehouseItem> warehouseItems =
                warehouseItemRepository.findAllByZoneIdAndNameOrSkuContaining(
                        warehouseZone.getId(), "RANDOM-SKU"
                );

        assertEquals(0, warehouseItems.size());
    }
}

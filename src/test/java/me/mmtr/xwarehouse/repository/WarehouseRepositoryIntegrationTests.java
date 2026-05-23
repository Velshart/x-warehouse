package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseMember;
import me.mmtr.xwarehouse.model.enums.WarehouseRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class WarehouseRepositoryIntegrationTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WarehouseRepository warehouseRepository;

    private User owner;
    private User member;
    private User stranger;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "password");
        member = new User("member", "password");
        stranger = new User("stranger", "password");

        entityManager.persist(owner);
        entityManager.persist(member);
        entityManager.persist(stranger);

        warehouse = new Warehouse("Main Warehouse", owner);
        entityManager.persist(warehouse);

        WarehouseMember membership = new WarehouseMember(warehouse, member, WarehouseRole.VIEWER);
        entityManager.persist(membership);

        entityManager.flush();
    }

    @Test
    void verifyIfAllAccessibleWarehousesForGivenUserAreReturned() {
        Warehouse otherWarehouse =
                new Warehouse("Other Warehouse", stranger);

        entityManager.persist(otherWarehouse);
        entityManager.flush();

        List<Warehouse> ownerWarehouses =
                warehouseRepository.findAllAccessibleByUserId(owner.getId());

        List<Warehouse> memberWarehouses =
                warehouseRepository.findAllAccessibleByUserId(member.getId());

        assertEquals(1, ownerWarehouses.size());
        assertEquals(
                "Main Warehouse",
                ownerWarehouses.getFirst().getName()
        );

        assertEquals(1, memberWarehouses.size());
        assertEquals(
                "Main Warehouse",
                memberWarehouses.getFirst().getName()
        );
    }

    @Nested
    class FindWarehouseByIdAndUserIdTests {

        @Test
        void verifyIfWarehouseWasReturnedWhenUserIsAnOwner() {
            Optional<Warehouse> foundWarehouse = warehouseRepository
                    .findWarehouseByIdAndUserId(
                            warehouse.getId(),
                            owner.getId()
                    );

            assertTrue(foundWarehouse.isPresent());
            assertEquals(warehouse.getId(), foundWarehouse.get().getId());
            assertEquals(
                    "Main Warehouse",
                    foundWarehouse.get().getName()
            );
        }

        @Test
        void verifyIfWarehouseWasReturnedWhenUserIsAMember() {
            Optional<Warehouse> foundWarehouse = warehouseRepository
                    .findWarehouseByIdAndUserId(
                            warehouse.getId(),
                            member.getId()
                    );

            assertTrue(foundWarehouse.isPresent());
            assertEquals(warehouse.getId(), foundWarehouse.get().getId());
        }

        @Test
        void verifyIfEmptyOptionalIsReturnedWhenUserHasNoAccessToWarehouse() {
            Optional<Warehouse> foundWarehouse = warehouseRepository
                    .findWarehouseByIdAndUserId(
                            warehouse.getId(),
                            stranger.getId()
                    );

            assertTrue(foundWarehouse.isEmpty());
        }

        @Test
        void verifyIfEmptyOptionalIsReturnedWhenWarehouseDoesNotExist() {
            Optional<Warehouse> foundWarehouse = warehouseRepository
                    .findWarehouseByIdAndUserId(999L, owner.getId());

            assertTrue(foundWarehouse.isEmpty());
        }
    }
}
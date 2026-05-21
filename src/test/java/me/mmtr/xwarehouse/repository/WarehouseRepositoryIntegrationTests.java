package me.mmtr.xwarehouse.repository;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseMember;
import me.mmtr.xwarehouse.model.enums.WarehouseRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class WarehouseRepositoryIntegrationTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Test
    void verifyIfAllAccessibleWarehousesForGivenUserAreReturned() {
        User firstUser = new User("owner", "password");
        User secondUser = new User("member", "password");
        User thirdUser = new User("other", "password");

        entityManager.persist(firstUser);
        entityManager.persist(secondUser);
        entityManager.persist(thirdUser);

        Warehouse warehouse1 = new Warehouse("Warehouse 1", firstUser);
        entityManager.persist(warehouse1);

        Warehouse warehouse2 = new Warehouse("Warehouse 2", thirdUser);
        entityManager.persist(warehouse2);

        WarehouseMember membership =
                new WarehouseMember(warehouse2, secondUser, WarehouseRole.VIEWER);
        entityManager.persist(membership);

        Warehouse warehouse3 = new Warehouse("Warehouse 3", thirdUser);
        entityManager.persist(warehouse3);

        entityManager.flush();

        List<Warehouse> firstUserWarehouses =
                warehouseRepository.findAllAccessibleByUserId(firstUser.getId());

        List<Warehouse> secondUserWarehouses =
                warehouseRepository.findAllAccessibleByUserId(secondUser.getId());

        assertEquals(1, firstUserWarehouses.size());

        assertEquals(1, secondUserWarehouses.size());
    }
}

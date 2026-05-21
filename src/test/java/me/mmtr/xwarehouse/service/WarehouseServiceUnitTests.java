package me.mmtr.xwarehouse.service;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseMember;
import me.mmtr.xwarehouse.model.enums.WarehouseRole;
import me.mmtr.xwarehouse.repository.UserRepository;
import me.mmtr.xwarehouse.repository.WarehouseMemberRepository;
import me.mmtr.xwarehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarehouseServiceUnitTests {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WarehouseMemberRepository warehouseMemberRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    private Warehouse warehouse;
    private User owner;
    private User regularUser;

    @BeforeEach
    public void setup() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("ownerUser");

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("regularUser");

        warehouse = new Warehouse("Test Warehouse", owner);
        warehouse.setId(3L);
    }

    @Nested
    class createAndGetWarehouseTests {
        @Test
        public void verifyIfWarehouseWasCreatedCorrectly() {
            when(warehouseRepository.save(any(Warehouse.class)))
                    .thenAnswer(invocationOnMock ->
                            invocationOnMock.getArgument(0)
                    );

            Warehouse createdWarehouse =
                    warehouseService.createWarehouse("Warehouse", owner);

            assertNotNull(createdWarehouse);
            assertEquals("Warehouse", createdWarehouse.getName());
            assertEquals(owner, createdWarehouse.getOwner());

            verify(
                    warehouseRepository,
                    times(1)
            ).save(any(Warehouse.class));
        }

        @Test
        public void verifyIfAvailableWarehousesWereReturnedCorrectly() {
            List<Warehouse> warehouses = List.of(warehouse);

            when(warehouseRepository.findAllAccessibleByUserId(owner.getId()))
                    .thenReturn(warehouses);

            List<Warehouse> availableWarehouses =
                    warehouseService.getAvailableWarehouses(owner.getId());

            assertEquals(1, availableWarehouses.size());
            assertEquals(
                    warehouse.getName(),
                    availableWarehouses.getFirst().getName()
            );

            verify(
                    warehouseRepository,
                    times(1)
            ).findAllAccessibleByUserId(owner.getId());
        }
    }

    @Nested
    class AddMemberTests {
        @Test
        public void verifyIfMemberWasAddedCorrectly() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(userRepository.findByUsername(regularUser.getUsername()))
                    .thenReturn(Optional.of(regularUser));

            when(warehouseMemberRepository
                    .findByWarehouseIdAndUserId(warehouse.getId(), regularUser.getId()))
                    .thenReturn(Optional.empty());

            assertDoesNotThrow(() ->
                    warehouseService.addMember(
                            warehouse.getId(),
                            regularUser.getUsername(),
                            WarehouseRole.ADMIN, owner)
            );

            verify(
                    warehouseMemberRepository,
                    times(1)
            ).save(any(WarehouseMember.class));
        }

        @Test
        void verifyIfExceptionIsThrownWhenWarehouseDoesNotExist() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.empty());

            IllegalArgumentException exception =
                    assertThrows(IllegalArgumentException.class, () ->
                            warehouseService.addMember(
                                    warehouse.getId(),
                                    regularUser.getUsername(),
                                    WarehouseRole.ADMIN,
                                    owner
                            )
                    );

            assertEquals(
                    "Warehouse with id 3 does not exist",
                    exception.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenCallerIsNotWarehouseOwner() {
            User notOwner = new User();
            notOwner.setId(4L);

            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            IllegalArgumentException exception =
                    assertThrows(IllegalArgumentException.class, () ->
                            warehouseService.addMember(
                                    warehouse.getId(),
                                    regularUser.getUsername(),
                                    WarehouseRole.ADMIN,
                                    notOwner
                            )
                    );

            assertEquals(
                    "Current user is not the owner of this warehouse",
                    exception.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenTargetUserDoesNotExist() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(userRepository.findByUsername("nonExistingUser"))
                    .thenReturn(Optional.empty());

            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () ->
                            warehouseService.addMember(
                                    warehouse.getId(),
                                    "nonExistingUser",
                                    WarehouseRole.VIEWER,
                                    owner
                            )
                    );

            assertEquals(
                    "Username nonExistingUser does not exist",
                    ex.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenTargetUserIsAnOwner() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(userRepository.findByUsername(owner.getUsername()))
                    .thenReturn(Optional.of(owner));

            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () ->
                            warehouseService.addMember(
                                    warehouse.getId(),
                                    owner.getUsername(),
                                    WarehouseRole.VIEWER,
                                    owner
                            )
                    );

            assertEquals(
                    "User ownerUser is already an owner of this warehouse",
                    ex.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenTargetUserIsAlreadyAMember() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(userRepository.findByUsername(regularUser.getUsername()))
                    .thenReturn(Optional.of(regularUser));

            WarehouseMember member = new WarehouseMember(
                    warehouse,
                    regularUser,
                    WarehouseRole.VIEWER
            );

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    regularUser.getId())
            ).thenReturn(Optional.of(member));

            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () ->
                            warehouseService.addMember(
                                    warehouse.getId(),
                                    regularUser.getUsername(),
                                    WarehouseRole.ADMIN,
                                    owner
                            )
                    );
            assertEquals(
                    "User regularUser is already a member" +
                            " of this warehouse",
                    ex.getMessage()
            );
        }
    }

    @Nested
    class DeleteWarehouseTests {
        @Test
        void verifyIfWarehouseWasDeletedCorrectly() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            assertDoesNotThrow(() ->
                    warehouseService.deleteWarehouse(warehouse.getId(), owner));

            verify(warehouseRepository, times(1))
                    .delete(warehouse);
        }

        @Test
        void verifyIfExceptionIsThrownWhenWarehouseDoesNotExist() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.empty());

            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () ->
                            warehouseService.deleteWarehouse(warehouse.getId(), owner));

            assertEquals(
                    "Warehouse with id 3 does not exist",
                    ex.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenCurrentUserIsNotAnOwnerOfTheWarehouse() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            IllegalStateException ex =
                    assertThrows(IllegalStateException.class, () ->
                            warehouseService.deleteWarehouse(
                                    warehouse.getId(),
                                    regularUser
                            )
                    );

            assertEquals(
                    "Current user is not the owner of this warehouse",
                    ex.getMessage()
            );
        }
    }

    @Nested
    class HasAccessTests {
        @Test
        void verifyIfFalseIsReturnedWhenWarehouseDoesNotExistAndTrueOtherwise() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.empty());

            assertFalse(warehouseService.hasAccess(
                    warehouse.getId(),
                    owner.getId(),
                    false
            ));

            assertFalse(warehouseService.hasAccess(
                    warehouse.getId(),
                    owner.getId(),
                    true
            ));
        }

        @Test
        void verifyIfTrueIsReturnedWhenCurrentUserIsAnOwnerOfTheWarehouseAndFalseOtherwise() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            assertTrue(warehouseService.hasAccess(
                    warehouse.getId(),
                    owner.getId(),
                    false
            ));

            assertTrue(warehouseService.hasAccess(
                    warehouse.getId(),
                    owner.getId(),
                    true
            ));
        }

        @Test
        void verifyIfTrueIsReturnedWhenUserIsViewerAndNoWriteAccessIsRequired() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            WarehouseMember member = new WarehouseMember(
                    warehouse,
                    regularUser,
                    WarehouseRole.VIEWER
            );

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    regularUser.getId()
            )).thenReturn(Optional.of(member));

            assertTrue(warehouseService.hasAccess(
                    warehouse.getId(),
                    regularUser.getId(),
                    false
            ));
        }

        @Test
        void verifyIfFalseIsReturnedWhenUserIsViewerAndWriteAccessIsRequired() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            WarehouseMember member = new WarehouseMember(
                    warehouse,
                    regularUser,
                    WarehouseRole.VIEWER
            );

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    regularUser.getId()
            )).thenReturn(Optional.of(member));

            assertFalse(warehouseService.hasAccess(
                    warehouse.getId(),
                    regularUser.getId(),
                    true
            ));
        }

        @Test
        void verifyIfTrueIsAlwaysReturnedWhenUserIsAdmin() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            WarehouseMember member = new WarehouseMember(
                    warehouse,
                    regularUser,
                    WarehouseRole.ADMIN
            );

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    regularUser.getId()
            )).thenReturn(Optional.of(member));

            assertTrue(warehouseService.hasAccess(
                    warehouse.getId(),
                    regularUser.getId(),
                    false
            ));

            assertTrue(warehouseService.hasAccess(
                    warehouse.getId(),
                    regularUser.getId(),
                    true
            ));
        }

        @Test
        void verifyIfFalseIsReturnedWhenUserIsNotAMember() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    regularUser.getId()
            )).thenReturn(Optional.empty());

            assertFalse(warehouseService.hasAccess(warehouse.getId(),
                    regularUser.getId(),
                    true
            ));

            assertFalse(warehouseService.hasAccess(
                    warehouse.getId(),
                    regularUser.getId(),
                    false
            ));
        }
    }
}

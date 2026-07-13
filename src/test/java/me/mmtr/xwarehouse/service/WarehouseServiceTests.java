package me.mmtr.xwarehouse.service;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
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
public class WarehouseServiceTests {

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
    class GetWarehouseByIdAndUserTests {
        @Test
        void verifyIfWarehouseIsReturnedCorrectly() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(
                    Optional.of(warehouse)
            );

            Warehouse foundWarehouse = warehouseService.getWarehouseByIdAndUser(
                    warehouse.getId(),
                    owner
            );

            assertNotNull(foundWarehouse);
            assertEquals(warehouse.getId(), foundWarehouse.getId());
        }

        @Test
        void verifyIfExceptionIsThrownWhenWarehouseNotFound() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(
                    Optional.empty()
            );

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            warehouseService.getWarehouseByIdAndUser(
                                    warehouse.getId(),
                                    owner
                            ));

            assertEquals(
                    "Warehouse not found",
                    ex.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoReadAccess() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.empty());

            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseService.getWarehouseByIdAndUser(
                                    warehouse.getId(),
                                    owner
                            ));

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex.getMessage()
            );
        }
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
        public void verifyIfAvailableWarehousesAreReturnedCorrectlyWithValidKeyword() {
            List<Warehouse> warehouses = List.of(warehouse);
            when(warehouseRepository.findAllAccessibleByUserIdAndNameContaining(
                    owner.getId(), warehouse.getName())
            ).thenReturn(warehouses);

            List<Warehouse> availableWarehouses =
                    warehouseService.getAvailableWarehouses(
                            owner.getId(), warehouse.getName()
                    );

            assertEquals(1, availableWarehouses.size());
            assertEquals(warehouse.getId(), availableWarehouses.getFirst().getId());

            verify(warehouseRepository, times(1))
                    .findAllAccessibleByUserIdAndNameContaining(
                            owner.getId(),
                            warehouse.getName()
                    );
        }

        @Test
        public void verifyIfAvailableWarehousesAreReturnedCorrectlyWithInvalidKeyword() {
            List<Warehouse> warehouses = List.of(warehouse);
            when(warehouseRepository.findAllAccessibleByUserId(
                    owner.getId())
            ).thenReturn(warehouses);

            List<Warehouse> availableWarehouses =
                    warehouseService.getAvailableWarehouses(
                            owner.getId(), "      "
                    );

            assertEquals(1, availableWarehouses.size());
            assertEquals(warehouse.getId(), availableWarehouses.getFirst().getId());

            verify(warehouseRepository, times(1))
                    .findAllAccessibleByUserId(
                            owner.getId()
                    );
        }

        @Test
        public void verifyIfAvailableWarehousesAreReturnedCorrectlyWithNullKeyword() {
            List<Warehouse> warehouses = List.of(warehouse);
            when(warehouseRepository.findAllAccessibleByUserId(
                    owner.getId())
            ).thenReturn(warehouses);

            List<Warehouse> availableWarehouses =
                    warehouseService.getAvailableWarehouses(
                            owner.getId(), null
                    );

            assertEquals(1, availableWarehouses.size());
            assertEquals(warehouse.getId(), availableWarehouses.getFirst().getId());

            verify(warehouseRepository, times(1))
                    .findAllAccessibleByUserId(
                            owner.getId()
                    );
        }
    }

    @Nested
    class UpdateWarehouseTests {
        @Test
        void verifyIfWarehouseIsUpdatedCorrectly() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(Optional.of(warehouse));

            warehouseService.updateWarehouse(
                    warehouse.getId(), "New Name", owner
            );

            assertEquals("New Name", warehouse.getName());
            verify(
                    warehouseRepository,
                    times(1)
            ).save(warehouse);
        }

        @Test
        void verifyIfExceptionIsThrownWhenUserIsNotAnOwnerOfTheWarehouse() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), regularUser.getId()
            )).thenReturn(Optional.of(warehouse));

            WarehouseMember adminMember =
                    new WarehouseMember(warehouse, regularUser, WarehouseRole.ADMIN);
            when(warehouseMemberRepository
                    .findByWarehouseIdAndUserId(
                            warehouse.getId(),
                            regularUser.getId()
                    ))
                    .thenReturn(Optional.of(adminMember));

            ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                    () -> warehouseService.updateWarehouse(
                            warehouse.getId(), "New Name", regularUser
                    ));


            assertEquals(
                    "Current user is not the owner of this warehouse",
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

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(Optional.of(warehouse));

            assertDoesNotThrow(() ->
                    warehouseService.deleteWarehouse(warehouse.getId(), owner));

            verify(warehouseRepository, times(1))
                    .delete(warehouse);
        }

        @Test
        void verifyIfExceptionIsThrownWhenCurrentUserIsNotAnOwnerOfTheWarehouse() {
            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), regularUser.getId()
            )).thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            WarehouseMember adminMember = new WarehouseMember(warehouse, regularUser, WarehouseRole.ADMIN);
            when(warehouseMemberRepository.findByWarehouseIdAndUserId(warehouse.getId(), regularUser.getId()))
                    .thenReturn(Optional.of(adminMember));

            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
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
    class AddMemberTests {
        @Test
        public void verifyIfMemberWasAddedCorrectly() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(Optional.of(warehouse));

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
        void verifyIfExceptionIsThrownWhenCallerIsNotWarehouseOwner() {
            User notOwner = new User();
            notOwner.setId(4L);

            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            WarehouseMember member = new WarehouseMember(
                    warehouse,
                    notOwner,
                    WarehouseRole.VIEWER
            );

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    notOwner.getId())
            ).thenReturn(Optional.of(member));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), notOwner.getId()
            )).thenReturn(Optional.of(warehouse));

            ResourceAccessException exception =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseService.addMember(
                                    warehouse.getId(),
                                    regularUser.getUsername(),
                                    WarehouseRole.ADMIN,
                                    notOwner
                            )
                    );

            assertEquals(
                    "This user is not the owner of this warehouse",
                    exception.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenTargetUserDoesNotExist() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(Optional.of(warehouse));

            when(userRepository.findByUsername("nonExistingUser"))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            warehouseService.addMember(
                                    warehouse.getId(),
                                    "nonExistingUser",
                                    WarehouseRole.VIEWER,
                                    owner
                            )
                    );

            assertEquals(
                    "User not found",
                    ex.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenTargetUserIsAnOwner() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(userRepository.findByUsername(owner.getUsername()))
                    .thenReturn(Optional.of(owner));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(Optional.of(warehouse));

            BusinessValidationException ex =
                    assertThrows(BusinessValidationException.class, () ->
                            warehouseService.addMember(
                                    warehouse.getId(),
                                    owner.getUsername(),
                                    WarehouseRole.VIEWER,
                                    owner
                            )
                    );

            assertEquals(
                    "This user is already an owner of this warehouse",
                    ex.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenTargetUserIsAlreadyAMember() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(Optional.of(warehouse));

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

            BusinessValidationException ex =
                    assertThrows(BusinessValidationException.class, () ->
                            warehouseService.addMember(
                                    warehouse.getId(),
                                    regularUser.getUsername(),
                                    WarehouseRole.ADMIN,
                                    owner
                            )
                    );
            assertEquals(
                    "This user is already a member" +
                            " of this warehouse",
                    ex.getMessage()
            );
        }
    }

    @Nested
    class RemoveMemberTests {
        @Test
        void verifyIfMemberWasRemovedSuccessfullyByOwner() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(Optional.of(warehouse));

            WarehouseMember member = new WarehouseMember(
                    warehouse,
                    regularUser,
                    WarehouseRole.VIEWER
            );

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    regularUser.getId()
            )).thenReturn(Optional.of(member));

            assertDoesNotThrow(() -> warehouseService.removeMember(warehouse.getId(),
                    regularUser.getId(), owner));

            verify(
                    warehouseMemberRepository,
                    times(1)
            ).delete(member);
        }

        @Test
        void verifyIfUserCanRemoveThemselves() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), regularUser.getId()
            )).thenReturn(Optional.of(warehouse));

            WarehouseMember member = new WarehouseMember(
                    warehouse,
                    regularUser,
                    WarehouseRole.VIEWER
            );

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    regularUser.getId()
            )).thenReturn(Optional.of(member));

            assertDoesNotThrow(() -> warehouseService.removeMember(
                    warehouse.getId(),
                    regularUser.getId(),
                    regularUser
            ));

            verify(
                    warehouseMemberRepository,
                    times(1)
            ).delete(member);
        }

        @Test
        void verifyIfAdminCanRemoveViewer() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            User admin = new User();
            admin.setId(4L);
            admin.setUsername("adminUser");

            WarehouseMember adminMember = new WarehouseMember(
                    warehouse,
                    admin,
                    WarehouseRole.ADMIN
            );

            WarehouseMember regularMember = new WarehouseMember(
                    warehouse, regularUser, WarehouseRole.VIEWER
            );

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    admin.getId())
            ).thenReturn(Optional.of(adminMember));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), admin.getId()
            )).thenReturn(Optional.of(warehouse));

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    regularUser.getId())
            ).thenReturn(Optional.of(regularMember));

            assertDoesNotThrow(() -> warehouseService.removeMember(
                    warehouse.getId(),
                    regularUser.getId(),
                    admin
            ));

            verify(
                    warehouseMemberRepository,
                    times(1)
            ).delete(regularMember);
        }

        @Test
        void verifyIfExceptionIsThrownWhenTargetUserIsAnOwner() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(Optional.of(warehouse));

            BusinessValidationException exception =
                    assertThrows(BusinessValidationException.class,
                            () -> warehouseService.removeMember(
                                    warehouse.getId(),
                                    owner.getId(),
                                    owner
                            )
                    );

            assertEquals(
                    "An owner cannot be removed from members list.",
                    exception.getMessage());
        }

        @Test
        void verifyIfExceptionIsThrownWhenAdminTriesToRemoveAnotherAdmin() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            User currentAdmin = new User();
            currentAdmin.setId(4L);

            User targetAdmin = new User();
            targetAdmin.setId(5L);

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), currentAdmin.getId()
            )).thenReturn(Optional.of(warehouse));

            WarehouseMember currentAdminMember = new WarehouseMember(
                    warehouse,
                    currentAdmin,
                    WarehouseRole.ADMIN
            );

            WarehouseMember targetAdminMember = new WarehouseMember(
                    warehouse,
                    targetAdmin,
                    WarehouseRole.ADMIN
            );

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    currentAdmin.getId()
            )).thenReturn(Optional.of(currentAdminMember));

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    targetAdmin.getId()
            )).thenReturn(Optional.of(targetAdminMember));

            ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                    () -> warehouseService.removeMember(
                            warehouse.getId(),
                            targetAdmin.getId(),
                            currentAdmin
                    )
            );

            assertEquals(
                    "Current user is not permitted to perform this operation",
                    ex.getMessage()
            );

            verify(warehouseMemberRepository, never()).delete(any());
        }

        @Test
        void verifyIfExceptionIsThrownWhenViewerTriesToRemoveAnotherUser() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            User currentViewer = new User();
            currentViewer.setId(4L);

            User targetViewer = new User();
            targetViewer.setId(5L);

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), currentViewer.getId()
            )).thenReturn(Optional.of(warehouse));

            WarehouseMember currentViewerMember = new WarehouseMember(
                    warehouse,
                    currentViewer,
                    WarehouseRole.VIEWER
            );

            WarehouseMember targetViewerMember = new WarehouseMember(
                    warehouse,
                    targetViewer,
                    WarehouseRole.VIEWER
            );

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    currentViewer.getId()
            )).thenReturn(Optional.of(currentViewerMember));

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    targetViewer.getId()
            )).thenReturn(Optional.of(targetViewerMember));

            ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                    () -> warehouseService.removeMember(
                            warehouse.getId(),
                            targetViewer.getId(),
                            currentViewer
                    )
            );

            assertEquals(
                    "Current user is not permitted to perform this operation",
                    ex.getMessage());
            verify(warehouseMemberRepository, never()).delete(any());
        }

        @Test
        void verifyIfExceptionIsThrownWhenTargetUserIsNotAMemberOfTheWarehouse() {
            when(warehouseRepository.findById(warehouse.getId()))
                    .thenReturn(Optional.of(warehouse));

            when(warehouseRepository.findWarehouseByIdAndUserId(
                    warehouse.getId(), owner.getId()
            )).thenReturn(Optional.of(warehouse));

            when(warehouseMemberRepository.findByWarehouseIdAndUserId(
                    warehouse.getId(),
                    regularUser.getId())
            ).thenReturn(Optional.empty());

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            warehouseService.removeMember(
                                    warehouse.getId(),
                                    regularUser.getId(),
                                    owner
                            )
                    );

            assertEquals(
                    "This user is not a member of this warehouse",
                    ex.getMessage()
            );

            verify(warehouseMemberRepository, never()).delete(any());
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

            assertFalse(warehouseService.hasAccess(
                    warehouse.getId(),
                    regularUser.getId(),
                    false
            ));

            assertTrue(warehouseService.hasAccess(
                    warehouse.getId(),
                    owner.getId(),
                    true
            ));


            assertFalse(warehouseService.hasAccess(
                    warehouse.getId(),
                    regularUser.getId(),
                    false
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

    @Nested
    class ValidateReadWriteAccessTests {
        @BeforeEach
        void setUp() {
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
        }

        @Test
        void verifyIfNoExceptionIsThrownWhenUserHasAccess() {
            assertDoesNotThrow(() -> warehouseService.validateReadAccess(
                    warehouse.getId(),
                    owner
            ));

            assertDoesNotThrow(() -> warehouseService.validateReadAccess(
                    warehouse.getId(),
                    regularUser
            ));
        }

        @Test
        void verifyIfExceptionIsThrownWhenUserHasNoAccess() {
            User otherUser = new User();
            otherUser.setId(4L);

            ResourceAccessException ex1 =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseService.validateWriteAccess(
                                    warehouse.getId(),
                                    regularUser
                            ));

            ResourceAccessException ex2 =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseService.validateWriteAccess(
                                    warehouse.getId(),
                                    otherUser
                            ));

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex1.getMessage()
            );

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex2.getMessage()
            );
        }
    }
}

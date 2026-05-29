package me.mmtr.xwarehouse.service;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseItem;
import me.mmtr.xwarehouse.model.WarehouseZone;
import me.mmtr.xwarehouse.repository.WarehouseItemRepository;
import me.mmtr.xwarehouse.repository.WarehouseRepository;
import me.mmtr.xwarehouse.repository.WarehouseZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarehouseZoneServiceTests {

    @Mock
    private WarehouseZoneRepository zoneRepository;

    @Mock
    private WarehouseItemRepository itemRepository;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseZoneService zoneService;

    private User currentUser;

    private Warehouse testWarehouse;

    private WarehouseZone testZone;

    @BeforeEach
    public void setup() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testUser");

        testWarehouse = new Warehouse();
        testWarehouse.setId(2L);
        testWarehouse.setName("Warehouse 1");

        testZone = new WarehouseZone("Zone A", testWarehouse);
        testZone.setId(3L);
    }

    @Nested
    class GetZoneByIdAndUserTests {
        @Test
        void verifyIfZoneIsReturnedCorrectly() {
            when(warehouseService
                    .getWarehouseByIdAndUser(testWarehouse.getId(), currentUser))
                    .thenReturn(testWarehouse);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.of(testZone));

            WarehouseZone zone = zoneService
                    .getZoneByIdAndUser(
                            testWarehouse.getId(), testZone.getId(), currentUser
                    );

            assertNotNull(zone);
            assertEquals(3L, zone.getId());
        }

        @Test
        void verifyIfExceptionIsThrownWhenZoneDoesNotExist() {
            when(warehouseService
                    .getWarehouseByIdAndUser(testWarehouse.getId(), currentUser)
            ).thenReturn(testWarehouse);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            zoneService.getZoneByIdAndUser(
                                    testWarehouse.getId(), testZone.getId(), currentUser
                            )
                    );

            assertEquals("Zone with id 3 does not exist", ex.getMessage());
        }

        @Test
        void verifyIfExceptionIsThrownWhenZoneDoesNotBelongToSpecifiedWarehouse() {
            when(warehouseService
                    .getWarehouseByIdAndUser(testWarehouse.getId(), currentUser)
            ).thenReturn(testWarehouse);

            Warehouse otherWarehouse = new Warehouse();
            otherWarehouse.setId(10L);
            otherWarehouse.setName("Other Warehouse");

            testZone.setWarehouse(otherWarehouse);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.of(testZone));

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> zoneService.getZoneByIdAndUser(testWarehouse.getId(),
                            testZone.getId(), currentUser)
            );

            assertEquals(
                    "Requested zone does not belong to the specified warehouse",
                    ex.getMessage()
            );
        }
    }

    @Nested
    class AddZoneTests {
        @Test
        void verifyIfZoneIsAddedCorrectly() {
            when(warehouseService
                    .getWarehouseByIdAndUser(testWarehouse.getId(), currentUser)
            ).thenReturn(testWarehouse);

            when(warehouseService
                    .hasAccess(
                            testWarehouse.getId(),
                            currentUser.getId(),
                            true
                    )
            ).thenReturn(true);

            when(zoneRepository
                    .findByWarehouseIdAndName(testWarehouse.getId(), testZone.getName()))
                    .thenReturn(Optional.empty());

            zoneService.addZone(testWarehouse.getId(), testZone.getName(), currentUser);

            verify(zoneRepository, times(1))
                    .save(any(WarehouseZone.class));
        }

        @Test
        void verifyIfExceptionIsThrownWhenUserDoesNotHaveWriteAccessToCreateZone() {
            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(), currentUser))
                    .thenReturn(testWarehouse);

            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    zoneService.addZone(
                            testWarehouse.getId(),
                            "New Zone", currentUser
                    )
            );

            assertEquals(
                    "You are not permitted to perform modifications in this warehouse",
                    ex.getMessage()
            );
            verify(zoneRepository, never()).save(any());
        }

        @Test
        void verifyIfExceptionIsThrownWhenZoneNameAlreadyExistsInWarehouse() {
            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(), currentUser)
            ).thenReturn(testWarehouse);

            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(true);

            when(zoneRepository
                    .findByWarehouseIdAndName(testWarehouse.getId(), testZone.getName())
            ).thenReturn(Optional.of(testZone));

            assertThrows(BusinessValidationException.class, () ->
                    zoneService.addZone(
                            testWarehouse.getId(),
                            testZone.getName(),
                            currentUser
                    )
            );
            verify(zoneRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateZoneTests {
        @Test
        void verifyIfZoneIsUpdatedCorrectly() {
            when(warehouseService.hasAccess(testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.of(testZone));

            zoneService.updateZone(
                    testWarehouse.getId(),
                    testZone.getId(),
                    "New Name",
                    currentUser
            );

            assertEquals("New Name", testZone.getName());
            verify(zoneRepository, times(1))
                    .save(testZone);
        }

        @Test
        void verifyIfExceptionIsThrownWhenZoneDoesNotExist() {
            when(warehouseService.hasAccess(testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.of(testZone));

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> zoneService.updateZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            "New Name",
                            currentUser
                    )
            );

            assertEquals("Zone not found", ex.getMessage());
        }

        @Test
        void verifyIfExceptionIsThrownWhenZoneBelongsToADifferentWarehouse() {
            when(warehouseService.hasAccess(testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            Warehouse otherWarehouse = new Warehouse(
                    "Other Warehouse", currentUser
            );
            otherWarehouse.setId(100L);

            testZone.setWarehouse(otherWarehouse);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.of(testZone));

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> zoneService.updateZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            "New Name",
                            currentUser
                    )
            );

            assertEquals(
                    "Requested zone does not belong to the specified warehouse",
                    ex.getMessage()
            );
        }
    }

    @Nested
    class DeleteZoneTests {
        @Test
        void verifyIfZoneIsDeletedCorrectly() {
            when(warehouseService.hasAccess(testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.of(testZone));

            zoneService.deleteZone(
                    testWarehouse.getId(),
                    testZone.getId(),
                    currentUser
            );

            verify(zoneRepository, times(1)).delete(testZone);
        }

        @Test
        void verifyIfExceptionIsThrownWhenZoneDoesNotExist() {
            when(warehouseService.hasAccess(testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> zoneService.deleteZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            currentUser
                    )
            );

            assertEquals(
                    "Zone not found",
                    ex.getMessage()
            );
            verify(zoneRepository, never()).delete(testZone);
        }

        @Test
        void verifyIfExceptionIsThrownWhenZoneToBeDeletedBelongsToDifferentWarehouse() {
            Warehouse alternativeWarehouse = new Warehouse();
            alternativeWarehouse.setId(99L);

            testZone.setWarehouse(alternativeWarehouse);

            when(warehouseService.hasAccess(testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.of(testZone));

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            zoneService.deleteZone(
                                    testWarehouse.getId(),
                                    testZone.getId(),
                                    currentUser
                            )
                    );

            assertEquals(
                    "This Zone does not exist in this warehouse",
                    ex.getMessage());
            verify(zoneRepository, never()).delete(testZone);
        }
    }

    @Nested
    class AddItemToZoneTests {
        @Test
        void verifyIfNewItemIsSavedCorrectly() {
            when(warehouseService.hasAccess(testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            when(zoneRepository.findById(3L)).thenReturn(Optional.of(testZone));
            when(itemRepository
                    .findByWarehouseZoneWarehouseIdAndSku(
                            testWarehouse.getId(), "TEST-01"))
                    .thenReturn(Optional.empty());

            zoneService.addItemToZone(
                    testWarehouse.getId(),
                    testZone.getId(),
                    "New Item",
                    "TEST-01",
                    5L,
                    currentUser
            );

            verify(itemRepository, times(1))
                    .save(any(WarehouseItem.class));
        }

        @Test
        void verifyIfItemQuantityIsIncreasedWhenSkuAlreadyExists() {
            WarehouseItem existingItem = new WarehouseItem(
                    "Existing Item", "TEST-01", 10L, testZone
            );
            existingItem.setId(100L);

            when(warehouseService.hasAccess(
                    testWarehouse.getId(), currentUser.getId(), true))
                    .thenReturn(true);

            when(zoneRepository
                    .findById(testZone.getId())).thenReturn(Optional.of(testZone));

            when(itemRepository
                    .findByWarehouseZoneWarehouseIdAndSku(
                            testWarehouse.getId(), "TEST-01"))
                    .thenReturn(Optional.of(existingItem));

            zoneService.addItemToZone(
                    testWarehouse.getId(),
                    testZone.getId(),
                    "Existing Item",
                    "TEST-01",
                    5L,
                    currentUser
            );

            assertEquals(15L, existingItem.getQuantity());
            verify(itemRepository, times(1)).save(existingItem);
        }

        @Test
        void verifyIfExceptionIsThrownWhenUserHasNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    zoneService.addItemToZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            "Item",
                            "TEST-01",
                            5L,
                            currentUser
                    )
            );

            assertEquals(
                    "You are not permitted to perform modifications" +
                            " in this warehouse",
                    ex.getMessage()
            );
            verify(itemRepository, never()).save(any());
        }

        @Test
        void verifyIfExceptionIsThrownWhenZoneDoesNotExist() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(true);

            when(zoneRepository.findById(testZone.getId())).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class, () ->
                            zoneService.addItemToZone(
                                    testWarehouse.getId(),
                                    testZone.getId(),
                                    "Zone A",
                                    "TEST-01",
                                    5L,
                                    currentUser
                            )
            );

            assertEquals(
                    "Zone with name " + testZone.getName() + " does not exist in this warehouse",
                    ex.getMessage()
            );
            verify(itemRepository, never()).delete(any());

        }
    }

    @Nested
    class UpdateItemInZone {
        @Test
        void verifyIfItemIsUpdatedCorrectly() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(true);

            WarehouseItem itemToUpdate = new WarehouseItem(
                    "Item To Update", "TEST-01", 10L, testZone
            );
            itemToUpdate.setId(100L);

            when(itemRepository.findById(100L)).thenReturn(Optional.of(itemToUpdate));

            zoneService.updateItemInZone(
                    testWarehouse.getId(),
                    itemToUpdate.getId(),
                    "New Name",
                    "TEST-02",
                    5L,
                    currentUser
            );

            assertEquals("New Name", itemToUpdate.getName());
            assertEquals("TEST-02", itemToUpdate.getSku());
            assertEquals(5L, itemToUpdate.getQuantity());

            verify(
                    itemRepository,
                    times(1)
            ).save(itemToUpdate);
        }

        @Test
        void verifyIfExceptionIsThrownWhenItemDoesNotExist() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(true);

            when(itemRepository.findById(100L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () ->
                            zoneService.updateItemInZone(
                                    testWarehouse.getId(),
                                    100L,
                                    "New Name",
                                    "TEST-02",
                                    5L,
                                    currentUser
                            )
            );

            assertEquals(
                    "Item does not exist",
                    ex.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenItemBelongsToTheOtherWarehouse() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(true);

            Warehouse otherWarehouse = new Warehouse(
                    "Other Warehouse", currentUser
            );
            otherWarehouse.setId(200L);

            testZone.setWarehouse(otherWarehouse);

            WarehouseItem itemToUpdate = new WarehouseItem(
                    "Item To Update", "TEST-01", 10L, testZone
            );
            itemToUpdate.setId(100L);

            when(itemRepository.findById(100L))
                    .thenReturn(Optional.of(itemToUpdate));

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            zoneService.updateItemInZone(
                                    testWarehouse.getId(),
                                    100L,
                                    "New Name",
                                    "TEST-02",
                                    5L,
                                    currentUser
                            )
                    );

            assertEquals(
                    "Item does not belong to this warehouse",
                    ex.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenItemWithGivenSkuAlreadyExists() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(true);

            WarehouseItem itemToUpdate = new WarehouseItem(
                    "Item To Update", "TEST-01", 10L, testZone
            );
            itemToUpdate.setId(100L);

            when(itemRepository.findById(100L))
                    .thenReturn(Optional.of(itemToUpdate));

            WarehouseItem itemWithTheSameSku = new WarehouseItem(
                    "Item With The Same SKU",
                    "TEST-02",
                    10L,
                    testZone
            );
            itemWithTheSameSku.setId(101L);

            when(itemRepository.findByWarehouseZoneWarehouseIdAndSku(
                    testWarehouse.getId(), "TEST-02")
            ).thenReturn(Optional.of(itemWithTheSameSku));

            BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                    zoneService.updateItemInZone(
                            testWarehouse.getId(),
                            itemToUpdate.getId(),
                            "New Name",
                            "TEST-02",
                            5L,
                            currentUser
                    )
            );

            assertEquals(
                    "Item with SKU TEST-02 already exists",
                    ex.getMessage()
            );

        }
    }

    @Nested
    class RemoveItemFromZoneTests {
        @Test
        void verifyIfItemIsDeletedCorrectly() {
            WarehouseItem itemToDelete = new WarehouseItem(
                    "Item To Delete", "TEST-01", 10L, testZone
            );
            itemToDelete.setId(100L);

            when(warehouseRepository.findById(testWarehouse.getId()))
                    .thenReturn(Optional.of(testWarehouse));

            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            when(itemRepository.findById(100L)).thenReturn(Optional.of(itemToDelete));

            zoneService.removeItemFromZone(
                    testWarehouse.getId(), 100L, currentUser
            );

            verify(itemRepository, times(1))
                    .delete(itemToDelete);
        }

        @Test
        void verifyIfExceptionIsThrownWheWarehouseDoesNotExist() {
            when(warehouseRepository.findById(testWarehouse.getId()))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class, () ->
                            zoneService.removeItemFromZone(testWarehouse.getId(), 100L, currentUser)
            );

            assertEquals(
                    "Warehouse does not exist",
                    ex.getMessage()
            );
            verify(itemRepository, never()).delete(any());
        }

        @Test
        void verifyIfExceptionIsThrowsWhenItemDoesNotExist() {
            when(warehouseRepository.findById(testWarehouse.getId()))
                    .thenReturn(Optional.of(testWarehouse));

            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(true);

            when(itemRepository.findById(100L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class, () ->
                            zoneService.removeItemFromZone(testWarehouse.getId(), 100L, currentUser)
            );

            assertEquals(
                    "Item does not exist",
                    ex.getMessage()
            );
            verify(itemRepository, never()).delete(any());

        }

        @Test
        void verifyIfExceptionIsThrownWhenItemToBeDeletedBelongsToDifferentWarehouse() {
            Warehouse alternativeWarehouse = new Warehouse();
            alternativeWarehouse.setId(99L);

            WarehouseZone alternativeZone = new WarehouseZone(
                    "Other Zone", alternativeWarehouse
            );

            WarehouseItem mismatchItem = new WarehouseItem(
                    "Mismatch Item",
                    "TEST-01",
                    10L,
                    alternativeZone
            );

            mismatchItem.setId(100L);

            when(warehouseRepository.findById(testWarehouse.getId()))
                    .thenReturn(Optional.of(testWarehouse));

            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(true);

            when(itemRepository.findById(100L)).thenReturn(Optional.of(mismatchItem));

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            zoneService.removeItemFromZone(
                                    testWarehouse.getId(),
                                    100L,
                                    currentUser
                            )
                    );

            assertEquals(
                    "Item does not exist in this warehouse",
                    ex.getMessage()
            );
            verify(itemRepository, never()).delete(any());
        }
    }
}

package me.mmtr.xwarehouse.service;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseItem;
import me.mmtr.xwarehouse.model.WarehouseZone;
import me.mmtr.xwarehouse.repository.WarehouseItemRepository;
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
public class WarehouseItemServiceTests {

    @Mock
    WarehouseZoneService zoneService;

    @Mock
    private WarehouseItemRepository itemRepository;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseItemService itemService;

    private User currentUser;
    private Warehouse testWarehouse;
    private WarehouseZone testZone;
    private WarehouseItem testItem;

    @BeforeEach
    public void setup() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testUser");

        testWarehouse = new Warehouse("Warehouse 1", currentUser);
        testWarehouse.setId(2L);

        testZone = new WarehouseZone("Zone A", testWarehouse);
        testZone.setId(3L);

        testItem = new WarehouseItem();
        testItem.setId(4L);
        testItem.setName("Item A");
        testItem.setSku("TEST-01");
        testItem.setQuantity(10L);
        testItem.setWarehouseZone(testZone);
    }

    @Nested
    class getItemByIdAndWarehouseIdTests {

        @BeforeEach
        public void setUp() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    false
            )).thenReturn(true);
        }

        @Test
        void verifyIfItemIsCorrectlyReturned() {
            when(itemRepository.findByIdAndWarehouseZoneWarehouseId(
                    testItem.getId(), testWarehouse.getId()
            )).thenReturn(Optional.of(testItem));

            WarehouseItem item = itemService.getItemByIdAndWarehouseId(
                    testItem.getId(), testWarehouse.getId(), currentUser
            );

            assertNotNull(item);
            assertEquals(testItem.getId(), item.getId());
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoReadAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    false)
            ).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                    () -> itemService.getItemByIdAndWarehouseId(
                            testItem.getId(),
                            testWarehouse.getId(),
                            currentUser
                    )
            );

            assertEquals("You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoItemFound() {
            when(itemRepository.findByIdAndWarehouseZoneWarehouseId(
                    testItem.getId(), testWarehouse.getId()
            )).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> itemService.getItemByIdAndWarehouseId(
                            testItem.getId(),
                            testWarehouse.getId(),
                            currentUser)
            );

            assertEquals(
                    "Item with provided id not found",
                    ex.getMessage()
            );
        }
    }

    @Nested
    class GetItemsByWarehouseIdAndUserTests {

        @BeforeEach
        public void setUp() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    false
            )).thenReturn(true);
        }

        @Test
        void verifyIfItemsAreCorrectlyReturnedWithValidKeywordParameter() {
            when(itemRepository.findAllByZoneIdAndNameOrSkuContaining(
                    testZone.getId(), "Item"
            )).thenReturn(List.of(testItem));

            List<WarehouseItem> items = itemService.getItemsByWarehouseIdAndUser(
                    testWarehouse.getId(),
                    testZone.getId(),
                    "Item",
                    currentUser
            );

            assertEquals(1, items.size());
            assertEquals(testItem.getId(), items.getFirst().getId());

            verify(itemRepository, times(1))
                    .findAllByZoneIdAndNameOrSkuContaining(
                            testZone.getId(), "Item"
                    );
        }

        @Test
        void verifyIfItemsAreCorrectlyReturnedWithInvalidKeywordParameter() {
            when(itemRepository.findAllByWarehouseZoneId(
                    testZone.getId()
            )).thenReturn(List.of(testItem));

            List<WarehouseItem> items = itemService.getItemsByWarehouseIdAndUser(
                    testWarehouse.getId(),
                    testZone.getId(),
                    "   ",
                    currentUser
            );

            assertEquals(1, items.size());
            assertEquals(testItem.getId(), items.getFirst().getId());

            verify(itemRepository, times(1))
                    .findAllByWarehouseZoneId(
                            testZone.getId()
                    );
        }

        @Test
        void verifyIfItemsAreCorrectlyReturnedWithNullKeywordParameter() {
            when(itemRepository.findAllByWarehouseZoneId(
                    testZone.getId()
            )).thenReturn(List.of(testItem));

            List<WarehouseItem> items = itemService.getItemsByWarehouseIdAndUser(
                    testWarehouse.getId(),
                    testZone.getId(),
                    null,
                    currentUser
            );

            assertEquals(1, items.size());
            assertEquals(testItem.getId(), items.getFirst().getId());

            verify(itemRepository, times(1))
                    .findAllByWarehouseZoneId(
                            testZone.getId()
                    );
        }
    }

    @Nested
    class AddItemToZoneTests {
        @BeforeEach
        public void setUp() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);
        }

        @Test
        void verifyIfNewItemIsSavedCorrectly() {
            when(zoneService.getZoneByIdAndUser(
                    testWarehouse.getId(), testZone.getId(), currentUser))
                    .thenReturn(testZone);

            when(itemRepository
                    .findByWarehouseZoneWarehouseIdAndSku(
                            testWarehouse.getId(), testItem.getSku()))
                    .thenReturn(Optional.empty());

            itemService.addItemToZone(
                    testWarehouse.getId(),
                    testZone.getId(),
                    "New Item",
                    testItem.getSku(),
                    5L,
                    currentUser
            );

            verify(itemRepository, times(1))
                    .save(any(WarehouseItem.class));
        }

        @Test
        void verifyIfItemQuantityIsIncreasedWhenSkuAlreadyExists() {
            when(zoneService.getZoneByIdAndUser(
                    testWarehouse.getId(), testZone.getId(), currentUser))
                    .thenReturn(testZone);

            when(itemRepository
                    .findByWarehouseZoneWarehouseIdAndSku(
                            testWarehouse.getId(), testItem.getSku()))
                    .thenReturn(Optional.of(testItem));

            itemService.addItemToZone(
                    testWarehouse.getId(),
                    testZone.getId(),
                    testItem.getName(),
                    testItem.getSku(),
                    5L,
                    currentUser
            );

            assertEquals(15L, testItem.getQuantity());
            verify(itemRepository, times(1)).save(testItem);
        }

        @Test
        void verifyIfExceptionIsThrownWhenUserHasNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                    () -> itemService.addItemToZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            testItem.getName(),
                            testItem.getSku(),
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
    }

    @Nested
    class UpdateItemInZone {
        private WarehouseZone otherTestZone;

        @BeforeEach
        public void setup() {
            otherTestZone = new WarehouseZone("Zone B", testWarehouse);
            otherTestZone.setId(5L);

            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);
        }

        @Test
        void verifyIfItemIsUpdatedCorrectly() {
            when(itemRepository.findById(testItem.getId()))
                    .thenReturn(Optional.of(testItem));

            when(zoneService.getZoneByIdAndUser(
                    testWarehouse.getId(),
                    otherTestZone.getId(),
                    currentUser
            )).thenReturn(otherTestZone);

            itemService.updateItemInZone(
                    testWarehouse.getId(),
                    testItem.getId(),
                    "New Name",
                    "TEST-02",
                    5L,
                    otherTestZone.getId(),
                    currentUser
            );

            assertEquals("New Name", testItem.getName());
            assertEquals("TEST-02", testItem.getSku());
            assertEquals(5L, testItem.getQuantity());
            assertEquals(5L, testItem.getWarehouseZone().getId());

            verify(itemRepository, times(1)).save(testItem);
        }

        @Test
        void verifyIfExceptionIsThrownWhenUserHasNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                    () -> itemService.updateItemInZone(
                            testWarehouse.getId(),
                            testItem.getId(),
                            "New Name",
                            "TEST-02",
                            5L,
                            4L,
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
        void verifyIfExceptionIsThrownWhenItemDoesNotExist() {
            when(itemRepository.findById(testItem.getId()))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> itemService.updateItemInZone(
                            testWarehouse.getId(),
                            testItem.getId(),
                            "New Name",
                            "TEST-02",
                            5L,
                            4L,
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
            Warehouse otherWarehouse = new Warehouse(
                    "Other Warehouse", currentUser
            );
            otherWarehouse.setId(200L);

            testZone.setWarehouse(otherWarehouse);

            when(itemRepository.findById(testItem.getId()))
                    .thenReturn(Optional.of(testItem));

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            itemService.updateItemInZone(
                                    testWarehouse.getId(),
                                    testItem.getId(),
                                    "New Name",
                                    "TEST-02",
                                    5L,
                                    otherTestZone.getId(),
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
            when(itemRepository.findById(testItem.getId()))
                    .thenReturn(Optional.of(testItem));

            WarehouseItem itemWithTheSameSku = new WarehouseItem(
                    "Item With The Same SKU",
                    "TEST-02",
                    10L,
                    testZone
            );
            itemWithTheSameSku.setId(101L);

            when(zoneService.getZoneByIdAndUser(
                    testWarehouse.getId(),
                    otherTestZone.getId(),
                    currentUser
            )).thenReturn(otherTestZone);

            when(itemRepository.findByWarehouseZoneWarehouseIdAndSku(
                    testWarehouse.getId(), "TEST-02")
            ).thenReturn(Optional.of(itemWithTheSameSku));

            BusinessValidationException ex =
                    assertThrows(BusinessValidationException.class, () ->
                            itemService.updateItemInZone(
                                    testWarehouse.getId(),
                                    testItem.getId(),
                                    "New Name",
                                    "TEST-02",
                                    5L,
                                    otherTestZone.getId(),
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
        @BeforeEach
        public void setUp() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);
        }

        @Test
        void verifyIfItemIsDeletedCorrectly() {
            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    currentUser
            )).thenReturn(testWarehouse);

            when(itemRepository.findById(testItem.getId()))
                    .thenReturn(Optional.of(testItem));

            itemService.removeItemFromZone(
                    testWarehouse.getId(), testItem.getId(), currentUser
            );

            verify(itemRepository, times(1)).delete(testItem);
        }

        @Test
        void verifyIfExceptionIsThrownWhenUserHasNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                    () -> itemService.removeItemFromZone(
                            testWarehouse.getId(),
                            testItem.getId(),
                            currentUser
                    )
            );

            assertEquals(
                    "You are not permitted to perform modifications" +
                            " in this warehouse",
                    ex.getMessage()
            );
            verify(itemRepository, never()).delete(any());
        }

        @Test
        void verifyIfExceptionIsThrowsWhenItemDoesNotExist() {
            when(itemRepository.findById(testItem.getId()))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class, () ->
                            itemService.removeItemFromZone(
                                    testWarehouse.getId(),
                                    testItem.getId(),
                                    currentUser
                            )
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

            testItem.setWarehouseZone(alternativeZone);

            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    currentUser
            )).thenReturn(testWarehouse);

            when(itemRepository.findById(testItem.getId()))
                    .thenReturn(Optional.of(testItem));

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            itemService.removeItemFromZone(
                                    testWarehouse.getId(),
                                    testItem.getId(),
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
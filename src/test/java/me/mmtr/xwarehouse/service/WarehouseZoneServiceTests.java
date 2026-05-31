package me.mmtr.xwarehouse.service;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseZone;
import me.mmtr.xwarehouse.repository.WarehouseZoneRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarehouseZoneServiceTests {

    @Mock
    private WarehouseZoneRepository zoneRepository;

    @Mock
    private WarehouseService warehouseService;

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
            assertEquals(testZone.getId(), zone.getId());
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
                                    testWarehouse.getId(),
                                    testZone.getId(),
                                    currentUser
                            )
                    );

            assertEquals("Zone with id " + testZone.getId() + " does not exist", ex.getMessage());
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
    class GetZonesByWarehouseIdTests {
        private WarehouseZone secondTestZone;

        @BeforeEach
        public void setup() {
            secondTestZone = new WarehouseZone("Zone B", testWarehouse);
            secondTestZone.setId(4L);
        }

        @Test
        void verifyIfAllZonesAreReturnedCorrectlyWithValidKeywordAttribute() {
            when(zoneRepository.findAllByWarehouseIdAndNameContaining(
                    testWarehouse.getId(), "Zone"
            )).thenReturn(List.of(testZone, secondTestZone));

            List<WarehouseZone> zoneList = zoneService.getZonesByWarehouseId(
                    testWarehouse.getId(), "Zone", currentUser
            );

            assertEquals(2, zoneList.size());
            assertEquals(testZone.getId(), zoneList.getFirst().getId());
            assertEquals(secondTestZone.getId(), zoneList.get(1).getId());

            verify(warehouseService, times(1))
                    .getWarehouseByIdAndUser(testWarehouse.getId(), currentUser);

            verify(zoneRepository, times(1))
                    .findAllByWarehouseIdAndNameContaining(
                            testWarehouse.getId(), "Zone"
                    );
        }

        @Test
        void verifyIfAllZonesAreReturnedCorrectlyWithInvalidAttribute() {
            when(zoneRepository.findAllByWarehouseId(testWarehouse.getId()))
                    .thenReturn(List.of(testZone, secondTestZone));

            List<WarehouseZone> zoneList = zoneService.getZonesByWarehouseId(
                    testWarehouse.getId(), "    ", currentUser
            );

            assertEquals(2, zoneList.size());
            assertEquals(testZone.getId(), zoneList.getFirst().getId());
            assertEquals(secondTestZone.getId(), zoneList.get(1).getId());

            verify(warehouseService, times(1))
                    .getWarehouseByIdAndUser(testWarehouse.getId(), currentUser);

            verify(zoneRepository, times(1))
                    .findAllByWarehouseId(testWarehouse.getId()
            );
        }


        @Test
        void verifyIfAllZonesAreReturnedCorrectlyWithNullKeywordAttribute() {
            when(zoneRepository.findAllByWarehouseId(testWarehouse.getId()))
                    .thenReturn(List.of(testZone, secondTestZone));

            List<WarehouseZone> zoneList = zoneService.getZonesByWarehouseId(
                    testWarehouse.getId(), null, currentUser
            );

            assertEquals(2, zoneList.size());
            assertEquals(testZone.getId(), zoneList.getFirst().getId());
            assertEquals(secondTestZone.getId(), zoneList.get(1).getId());

            verify(warehouseService, times(1))
                    .getWarehouseByIdAndUser(testWarehouse.getId(), currentUser);

            verify(zoneRepository, times(1))
                    .findAllByWarehouseId(testWarehouse.getId()
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
                    .findByWarehouseIdAndName(
                            testWarehouse.getId(),
                            testZone.getName()
                    ))
                    .thenReturn(Optional.empty());

            zoneService.addZone(
                    testWarehouse.getId(),
                    testZone.getName(),
                    currentUser
            );

            verify(zoneRepository, times(1))
                    .save(any(WarehouseZone.class));
        }

        @Test
        void verifyIfExceptionIsThrownWhenUserHasNoWriteAccess() {
            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(), currentUser))
                    .thenReturn(testWarehouse);

            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                    () -> zoneService.addZone(
                            testWarehouse.getId(),
                            "New Zone",
                            currentUser
                    )
            );

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
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
                    .findByWarehouseIdAndName(
                            testWarehouse.getId(),
                            testZone.getName()
                    )
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
        void verifyIfExceptionIsThrownWhenUserHasNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                    () -> zoneService.updateZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            "New Name",
                            currentUser
                    )
            );

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex.getMessage()
            );
            verify(zoneRepository, never()).save(any());
        }

        @Test
        void verifyIfExceptionIsThrownWhenZoneDoesNotExist() {
            when(warehouseService.hasAccess(testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class,
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

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class,
                            () -> zoneService.updateZone(
                                    testWarehouse.getId(),
                                    testZone.getId(),
                                    "New Name",
                                    currentUser
                            )
                    );

            assertEquals(
                    "Requested zone does not belong" +
                            " to the specified warehouse",
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

            verify(
                    zoneRepository,
                    times(1)
            ).delete(testZone);
        }

        @Test
        void verifyIfExceptionIsThrownWhenUserHasNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true)
            ).thenReturn(false);

            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            zoneService.deleteZone(
                                    testWarehouse.getId(),
                                    testZone.getId(),
                                    currentUser
                            )
                    );

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex.getMessage()
            );
            verify(zoneRepository, never()).delete(any());
        }

        @Test
        void verifyIfExceptionIsThrownWhenZoneDoesNotExist() {
            when(warehouseService.hasAccess(testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            when(zoneRepository.findById(testZone.getId()))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class,
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
                    "Requested zone does not belong" +
                            " to the specified warehouse",
                    ex.getMessage());
            verify(zoneRepository, never()).delete(testZone);
        }
    }
}
package me.mmtr.xwarehouse.service;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseContractor;
import me.mmtr.xwarehouse.repository.WarehouseContractorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarehouseContractorServiceTests {

    @Mock
    private WarehouseContractorRepository contractorRepository;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseContractorService contractorService;

    private WarehouseContractor testContractor;
    private Warehouse testWarehouse;
    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User("Test User", "Password");
        testUser.setId(1L);

        testWarehouse = new Warehouse("Test Warehouse", testUser);
        testWarehouse.setId(2L);

        testContractor = new WarehouseContractor(
                "TEST-CODE",
                "TEST",
                "0123456789",
                "ADDITIONAL INFO",
                testWarehouse

        );
        testContractor.setId(3L);
    }

    @Nested
    class GetContractorsByWarehouseIdTests {
        @Test
        void verifyIfContractorsAreCorrectlyReturned() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    false
            )).thenReturn(true);

            when(contractorRepository.findAllByWarehouseId(testWarehouse.getId()))
                    .thenReturn(List.of(testContractor));

            List<WarehouseContractor> contractors =
                    contractorService.getContractorsByWarehouseId(
                            testWarehouse.getId(),
                            testUser
                    );

            assertEquals(1, contractors.size());
            assertEquals(testContractor.getId(), contractors.getFirst().getId());
        }

        @Test
        void verifyIfResourceAccessExceptionIsThrownWhenNoReadAccess() {
            when(warehouseService.hasAccess(testWarehouse.getId(), testUser.getId(), false))
                    .thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    contractorService.getContractorsByWarehouseId(testWarehouse.getId(), testUser)
            );

            verify(contractorRepository, never()).findAllByWarehouseId(any());
            assertEquals("You are not permitted to perform" +
                    " modifications in this warehouse", ex.getMessage()
            );
        }
    }

    @Nested
    class AddContractorToWarehouseTests {
        @Test
        void verifyIfNewContractorIsCorrectlyAddedToTheWarehouse() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(true);

            when(warehouseService.getWarehouseByIdAndUser(testWarehouse.getId(), testUser))
                    .thenReturn(testWarehouse);
            when(contractorRepository.findByWarehouseIdAndNip(testWarehouse.getId(), "0123456789"))
                    .thenReturn(Optional.empty());

            contractorService.addContractorToWarehouse(
                    testWarehouse.getId(), "TEST-CODE", "TEST", "0123456789", "ADDITIONAL INFO", testUser
            );

            verify(
                    contractorRepository,
                    times(1)
            ).save(any(WarehouseContractor.class));
        }

        @Test
        void verifyIfResourceAccessExceptionIsThrownWhenNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    contractorService.addContractorToWarehouse(
                            testWarehouse.getId(),
                            "CODE",
                            "NAME",
                            "123",
                            "INFO",
                            testUser
                    ));

            assertEquals("You are not permitted to perform" +
                    " modifications in this warehouse", ex.getMessage());
        }

        @Test
        void verifyIfBusinessValidationExceptionIsThrownWhenNipAlreadyExists() {
            when(warehouseService.hasAccess(testWarehouse.getId(), testUser.getId(), true))
                    .thenReturn(true);
            when(warehouseService.getWarehouseByIdAndUser(testWarehouse.getId(), testUser))
                    .thenReturn(testWarehouse);
            when(contractorRepository.findByWarehouseIdAndNip(testWarehouse.getId(), "0123456789"))
                    .thenReturn(Optional.of(testContractor));

            assertThrows(BusinessValidationException.class, () ->
                    contractorService.addContractorToWarehouse(
                            testWarehouse.getId(),
                            "TEST-CODE",
                            "TEST",
                            "0123456789",
                            "ADDITIONAL INFO",
                            testUser
                    )
            );

            verify(contractorRepository, never()).save(any());
        }
    }

    @Nested
    class RemoveContractorFromWarehouseTests {
        @Test
        void verifyIfContractorIsCorrectlyRemoved() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(true);

            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.of(testContractor));

            contractorService.removeContractorFromWarehouse(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    testUser
            );

            verify(
                    contractorRepository,
                    times(1)
            ).delete(testContractor);
        }

        @Test
        void verifyIfResourceAccessExceptionIsThrownWhenNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(false);

            assertThrows(ResourceAccessException.class, () ->
                    contractorService.removeContractorFromWarehouse(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            testUser
                    )
            );

            verify(contractorRepository, never()).delete(any());
        }

        @Test
        void verifyIfResourceNotFoundExceptionIsThrownWhenContractorDoesNotExist() {
            when(warehouseService.hasAccess(testWarehouse.getId(), testUser.getId(), true))
                    .thenReturn(true);
            when(contractorRepository.findByIdAndWarehouseId(testContractor.getId(), testWarehouse.getId()))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    contractorService.removeContractorFromWarehouse(testWarehouse.getId(), testContractor.getId(), testUser)
            );

            verify(contractorRepository, never()).delete(any());
        }
    }

    @Nested
    class UpdateContractorTests {
        @Test
        void verifyIfContractorIsSuccessfullyUpdatedWithoutChangingNip() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(true);

            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(), testWarehouse.getId()
            )).thenReturn(Optional.of(testContractor));

            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    testUser
            )).thenReturn(testWarehouse);

            contractorService.updateContractor(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    "NEW-CODE",
                    "NEW-NAME",
                    testContractor.getNip(),
                    "NEW-INFO",
                    testUser
            );

            assertEquals("NEW-CODE", testContractor.getCode());
            assertEquals("NEW-NAME", testContractor.getName());
            assertEquals("NEW-INFO", testContractor.getAdditionalInfo());
            verify(
                    contractorRepository,
                    times(1)
            ).save(testContractor);
        }

        @Test
        void verifyIfContractorIsSuccessfullyUpdatedWithNewUniqueNip() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(true);

            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(), testWarehouse.getId()
            )).thenReturn(Optional.of(testContractor));

            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    testUser
            )).thenReturn(testWarehouse);

            String newNip = "9876543210";

            contractorService.updateContractor(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    "NEW-CODE",
                    "NEW-NAME",
                    newNip,
                    "NEW-INFO",
                    testUser
            );

            assertEquals("NEW-CODE", testContractor.getCode());
            assertEquals("NEW-NAME", testContractor.getName());
            assertEquals("NEW-INFO", testContractor.getAdditionalInfo());
            assertEquals(newNip, testContractor.getNip());
            verify(
                    contractorRepository,
                    times(1)
            ).save(testContractor);
        }

        @Test
        void verifyIfBusinessValidationExceptionIsThrownWhenNewNipAlreadyExists() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(true);

            when(contractorRepository.findByIdAndWarehouseId(testContractor.getId(), testWarehouse.getId()))
                    .thenReturn(Optional.of(testContractor));
            when(warehouseService.getWarehouseByIdAndUser(testWarehouse.getId(), testUser))
                    .thenReturn(testWarehouse);

            String newNip = "9876543210";
            WarehouseContractor otherContractor = new WarehouseContractor();
            when(contractorRepository.findByWarehouseIdAndNip(
                    testWarehouse.getId(),
                    newNip
            )).thenReturn(Optional.of(otherContractor));

            BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                    contractorService.updateContractor(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            "CODE",
                            "NAME",
                            newNip,
                            "INFO",
                            testUser
                    )
            );

            assertEquals(
                    "Contractor with provided NIP " +
                            "already exists in this warehouse",
                    ex.getMessage()
            );
            verify(contractorRepository, never()).save(any());
        }

        @Test
        void verifyIfResourceAccessExceptionIsThrownWhenNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    contractorService.updateContractor(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            "NEW-CODE",
                            "NEW-NAME",
                            "NEW-NIP",
                            "NEW-INFO",
                            testUser
                    )
            );

            assertEquals("You are not permitted to perform" +
                    " modifications in this warehouse", ex.getMessage());
            verify(contractorRepository, never()).delete(any());
        }

        @Test
        void verifyIfResourceNotFoundExceptionIsThrownWhenContractorDoesNotExist() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(true);

            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                            contractorService.updateContractor(
                                    testWarehouse.getId(),
                                    testContractor.getId(),
                                    "NEW-CODE",
                                    "NEW-NAME",
                                    "NEW-NIP",
                                    "NEW-INFO",
                                    testUser
                            )
            );

            assertEquals(
                    "Contractor not found",
                    ex.getMessage()
            );
            verify(contractorRepository, never()).delete(any());
        }


    }

    @Nested
    class RemoveContractorFromWarehouse {

        @Test
        void verifyIfContractorIsCorrectlyRemoved() {
            when(warehouseService.hasAccess(testWarehouse.getId(), testUser.getId(), true))
                    .thenReturn(true);
            when(contractorRepository.findByIdAndWarehouseId(testContractor.getId(), testWarehouse.getId()))
                    .thenReturn(Optional.of(testContractor));

            contractorService.removeContractorFromWarehouse(testWarehouse.getId(), testContractor.getId(), testUser);

            verify(
                    contractorRepository,
                    times(1)
            ).delete(testContractor);
        }

        @Test
        void shouldThrowResourceAccessExceptionWhenNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    contractorService.removeContractorFromWarehouse(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            testUser
                    )
            );

            assertEquals(
                    "You are not permitted to perform" +
                    " modifications in this warehouse",
                    ex.getMessage()
            );
            verify(contractorRepository, never()).delete(any());
        }

        @Test
        void verifyIfResourceNotFoundExceptionIsThrownWhenContractorDoesNotExist() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(true);

            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    contractorService.removeContractorFromWarehouse(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            testUser
                    ));

            verify(contractorRepository, never()).delete(any());
        }
    }
}

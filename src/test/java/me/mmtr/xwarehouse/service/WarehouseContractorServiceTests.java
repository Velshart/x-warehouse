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

import static org.junit.jupiter.api.Assertions.*;
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
    private User currentUser;

    @BeforeEach
    public void setup() {
        currentUser = new User("Test User", "Password");
        currentUser.setId(1L);

        testWarehouse = new Warehouse("Test Warehouse", currentUser);
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
            when(contractorRepository.findAllByWarehouseId(testWarehouse.getId()))
                    .thenReturn(List.of(testContractor));

            List<WarehouseContractor> contractors =
                    contractorService.getContractorsByWarehouseId(
                            testWarehouse.getId(),
                            currentUser
                    );

            assertEquals(1, contractors.size());
            assertEquals(testContractor.getId(), contractors.getFirst().getId());
        }

        @Test
        void verifyIfResourceAccessExceptionIsThrownWhenNoReadAccess() {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateReadAccess(testWarehouse.getId(), currentUser);


            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    contractorService.getContractorsByWarehouseId(testWarehouse.getId(), currentUser)
            );

            verify(contractorRepository, never()).findAllByWarehouseId(any());
            assertEquals("You are not permitted to perform" +
                    " modifications in this warehouse", ex.getMessage()
            );
        }
    }

    @Nested
    class GetContractorByIdTests {
        @Test
        void verifyIfContractorIsCorrectlyReturned() {
            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(),
                    testWarehouse.getId())
            ).thenReturn(Optional.of(testContractor));

            WarehouseContractor contractor = contractorService.getContractorById(
                    testWarehouse.getId(), testContractor.getId(), currentUser
            );

            assertNotNull(contractor);
            assertEquals(testContractor.getId(), contractor.getId());
        }

        @Test
        void verifyIfExceptionIsThrownWhenContractorNotFound() {
            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(),
                    testWarehouse.getId())
            ).thenReturn(Optional.empty());

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            contractorService.getContractorById(
                                    testWarehouse.getId(),
                                    testContractor.getId(),
                                    currentUser
                            ));

            assertEquals(
                    "Contractor with provided id not found",
                    ex.getMessage()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoReadAccess() {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateReadAccess(testWarehouse.getId(), currentUser);


            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            contractorService.getContractorById(
                                    testWarehouse.getId(),
                                    testContractor.getId(),
                                    currentUser
                            ));

            assertEquals("You are not permitted to perform" +
                    " modifications in this warehouse", ex.getMessage()
            );
        }
    }

    @Nested
    class AddContractorToWarehouseTests {
        @Test
        void verifyIfNewContractorIsCorrectlyAddedToTheWarehouse() {
            when(warehouseService.getWarehouseByIdAndUser(testWarehouse.getId(), currentUser))
                    .thenReturn(testWarehouse);
            when(contractorRepository.findByWarehouseIdAndNip(testWarehouse.getId(), "0123456789"))
                    .thenReturn(Optional.empty());

            contractorService.addContractorToWarehouse(
                    testWarehouse.getId(), "TEST-CODE", "TEST", "0123456789", "ADDITIONAL INFO", currentUser
            );

            verify(
                    contractorRepository,
                    times(1)
            ).save(any(WarehouseContractor.class));
        }

        @Test
        void verifyIfResourceAccessExceptionIsThrownWhenNoWriteAccess() {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateWriteAccess(testWarehouse.getId(), currentUser);


            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    contractorService.addContractorToWarehouse(
                            testWarehouse.getId(),
                            "CODE",
                            "NAME",
                            "123",
                            "INFO",
                            currentUser
                    ));

            assertEquals("You are not permitted to perform" +
                    " modifications in this warehouse", ex.getMessage());
        }

        @Test
        void verifyIfBusinessValidationExceptionIsThrownWhenNipAlreadyExists() {
            when(warehouseService.getWarehouseByIdAndUser(testWarehouse.getId(), currentUser))
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
                            currentUser
                    )
            );

            verify(contractorRepository, never()).save(any());
        }
    }

    @Nested
    class RemoveContractorFromWarehouseTests {
        @Test
        void verifyIfContractorIsCorrectlyRemoved() {
            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.of(testContractor));

            contractorService.removeContractorFromWarehouse(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    currentUser
            );

            verify(
                    contractorRepository,
                    times(1)
            ).delete(testContractor);
        }

        @Test
        void verifyIfResourceAccessExceptionIsThrownWhenNoWriteAccess() {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateWriteAccess(testWarehouse.getId(), currentUser);


            assertThrows(ResourceAccessException.class, () ->
                    contractorService.removeContractorFromWarehouse(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            currentUser
                    )
            );

            verify(contractorRepository, never()).delete(any());
        }

        @Test
        void verifyIfResourceNotFoundExceptionIsThrownWhenContractorDoesNotExist() {
            when(contractorRepository.findByIdAndWarehouseId(testContractor.getId(), testWarehouse.getId()))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    contractorService.removeContractorFromWarehouse(testWarehouse.getId(), testContractor.getId(), currentUser)
            );

            verify(contractorRepository, never()).delete(any());
        }
    }

    @Nested
    class UpdateContractorTests {
        @Test
        void verifyIfContractorIsSuccessfullyUpdatedWithoutChangingNip() {
            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(), testWarehouse.getId()
            )).thenReturn(Optional.of(testContractor));

            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    currentUser
            )).thenReturn(testWarehouse);

            contractorService.updateContractor(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    "NEW-CODE",
                    "NEW-NAME",
                    testContractor.getNip(),
                    "NEW-INFO",
                    currentUser
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
            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(), testWarehouse.getId()
            )).thenReturn(Optional.of(testContractor));

            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    currentUser
            )).thenReturn(testWarehouse);

            String newNip = "9876543210";

            contractorService.updateContractor(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    "NEW-CODE",
                    "NEW-NAME",
                    newNip,
                    "NEW-INFO",
                    currentUser
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
            when(contractorRepository.findByIdAndWarehouseId(testContractor.getId(), testWarehouse.getId()))
                    .thenReturn(Optional.of(testContractor));
            when(warehouseService.getWarehouseByIdAndUser(testWarehouse.getId(), currentUser))
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
                            currentUser
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
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateWriteAccess(testWarehouse.getId(), currentUser);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    contractorService.updateContractor(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            "NEW-CODE",
                            "NEW-NAME",
                            "NEW-NIP",
                            "NEW-INFO",
                            currentUser
                    )
            );

            assertEquals("You are not permitted to perform" +
                    " modifications in this warehouse", ex.getMessage());
            verify(contractorRepository, never()).delete(any());
        }

        @Test
        void verifyIfResourceNotFoundExceptionIsThrownWhenContractorDoesNotExist() {
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
                            currentUser
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
            when(contractorRepository.findByIdAndWarehouseId(testContractor.getId(), testWarehouse.getId()))
                    .thenReturn(Optional.of(testContractor));

            contractorService.removeContractorFromWarehouse(testWarehouse.getId(), testContractor.getId(), currentUser);

            verify(
                    contractorRepository,
                    times(1)
            ).delete(testContractor);
        }

        @Test
        void shouldThrowResourceAccessExceptionWhenNoWriteAccess() {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateWriteAccess(testWarehouse.getId(), currentUser);


            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    contractorService.removeContractorFromWarehouse(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            currentUser
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
            when(contractorRepository.findByIdAndWarehouseId(
                    testContractor.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    contractorService.removeContractorFromWarehouse(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            currentUser
                    ));

            verify(contractorRepository, never()).delete(any());
        }
    }
}

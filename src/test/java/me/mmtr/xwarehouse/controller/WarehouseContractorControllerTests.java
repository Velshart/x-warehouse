package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseContractor;
import me.mmtr.xwarehouse.service.WarehouseContractorService;
import me.mmtr.xwarehouse.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseContractorController.class)
public class WarehouseContractorControllerTests {
    @MockitoBean
    private WarehouseService warehouseService;

    @MockitoBean
    private WarehouseContractorService contractorService;

    @Autowired
    private MockMvc mockMvc;

    private User testUser;
    private Warehouse testWarehouse;
    private WarehouseContractor testContractor;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testWarehouse = new Warehouse();
        testWarehouse.setId(2L);
        testWarehouse.setName("Central Warehouse");
        testWarehouse.setOwner(testUser);

        testContractor = new WarehouseContractor(
                "TEST-CODE",
                "TEST-NAME",
                "1234567890",
                "Some additional info",
                testWarehouse
        );
        testContractor.setId(3L);
    }

    @Nested
    class ContractorsGetTest {
        @Test
        void verifyIfManageContractorsViewAndModelAreCorrectlyReturned() throws Exception {
            List<WarehouseContractor> contractorsList = List.of(testContractor);

            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    testUser
            )).thenReturn(testWarehouse);
            when(contractorService.getContractorsByWarehouseId(
                    testWarehouse.getId(),
                    testUser
            )).thenReturn(contractorsList);
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    testUser.getId(),
                    true
            )).thenReturn(true);

            mockMvc.perform(get("/warehouse/" + testWarehouse.getId() +
                            "/contractors")
                            .with(user(testUser)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-contractors"))
                    .andExpect(model().attribute("warehouse", testWarehouse))
                    .andExpect(model().attribute(
                            "contractors",
                            contractorsList
                    ))
                    .andExpect(model().attribute("canManage", true))
                    .andExpect(model().attribute("currentUser", testUser));
        }
    }

    @Nested
    class AddContractorTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() +
                            "/contractors/add-contractor")
                            .param("code", testContractor.getCode())
                            .param("name", testContractor.getName())
                            .param("nip", testContractor.getNip())
                            .param(
                                    "additionalInfo",
                                    testContractor.getAdditionalInfo()
                            ).with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractors")
                    ).andExpect(flash().attribute(
                            "success",
                            "Contractor added successfully"
                    ));

            verify(contractorService, times(1))
                    .addContractorToWarehouse(
                            testWarehouse.getId(),
                            testContractor.getCode(),
                            testContractor.getName(),
                            testContractor.getNip(),
                            testContractor.getAdditionalInfo(),
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform " +
                            "modifications in this warehouse"
            )).when(contractorService).addContractorToWarehouse(
                    testWarehouse.getId(),
                    testContractor.getCode(),
                    testContractor.getName(),
                    testContractor.getNip(),
                    testContractor.getAdditionalInfo(),
                    testUser
            );

            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() +
                            "/contractors/add-contractor")
                            .param("code", testContractor.getCode())
                            .param("name", testContractor.getName())
                            .param("nip", testContractor.getNip())
                            .param(
                                    "additionalInfo",
                                    testContractor.getAdditionalInfo()
                            ).with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractors")
                    ).andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform modifications" +
                                    " in this warehouse"
                    ));
        }
    }


    @Nested
    class UpdateContractorTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() +
                            "/contractors/update-contractor")
                            .param(
                                    "contractorId",
                                    String.valueOf(testContractor.getId())
                            )
                            .param("code", "UPDATED-CODE")
                            .param("name", "Updated Name")
                            .param("nip", "0000000000")
                            .param("additionalInfo", "New Info")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" +
                            testWarehouse.getId() + "/contractors"
                    ))
                    .andExpect(flash().attribute(
                            "success",
                            "Contractor updated successfully"
                    ));

            verify(
                    contractorService,
                    times(1)
            ).updateContractor(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    "UPDATED-CODE",
                    "Updated Name",
                    "0000000000",
                    "New Info",
                    testUser
            );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceNotFoundException("Contractor not found"))
                    .when(contractorService).updateContractor(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            "UPDATED-CODE",
                            "Updated Name",
                            "0000000000",
                            "New Info",
                            testUser
                    );

            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() +
                            "/contractors/update-contractor")
                            .param(
                                    "contractorId",
                                    String.valueOf(testContractor.getId())
                            )
                            .param("code", "UPDATED-CODE")
                            .param("name", "Updated Name")
                            .param("nip", "0000000000")
                            .param("additionalInfo", "New Info")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" +
                            testWarehouse.getId() + "/contractors")
                    )
                    .andExpect(flash().attribute(
                            "error",
                            "Contractor not found"
                    ));
        }
    }

    @Nested
    class DeleteContractorTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() +
                            "/contractors/delete-contractor")
                            .param(
                                    "contractorId",
                                    String.valueOf(testContractor.getId())
                            )
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" +
                            testWarehouse.getId() +
                            "/contractors")
                    )
                    .andExpect(flash().attribute(
                            "success",
                            "Contractor deleted successfully"
                    ));

            verify(contractorService, times(1))
                    .removeContractorFromWarehouse(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(contractorService).removeContractorFromWarehouse(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    testUser
            );

            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() +
                            "/contractors/delete-contractor")
                            .param(
                                    "contractorId",
                                    String.valueOf(testContractor.getId())
                            )
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" +
                            testWarehouse.getId() + "/contractors")
                    )
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform" +
                                    " modifications in this warehouse"
                    ));
        }
    }
}

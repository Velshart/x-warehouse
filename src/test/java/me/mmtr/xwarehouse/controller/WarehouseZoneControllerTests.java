package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseZone;
import me.mmtr.xwarehouse.service.WarehouseService;
import me.mmtr.xwarehouse.service.WarehouseZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseZoneController.class)
public class WarehouseZoneControllerTests {

    @MockitoBean
    private WarehouseZoneService zoneService;

    @MockitoBean
    private WarehouseService warehouseService;

    @Autowired
    private MockMvc mockMvc;

    private User testUser;
    private Warehouse testWarehouse;
    private WarehouseZone testZone;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testWarehouse = new Warehouse();
        testWarehouse.setId(2L);
        testWarehouse.setName("Central Warehouse");
        testWarehouse.setOwner(testUser);

        testZone = new WarehouseZone("Zone A", testWarehouse);
        testZone.setId(3L);
    }

    @Nested
    class ViewZoneTests {
        @Test
        void verifyIfZoneViewIsReturnedCorrectly() throws Exception {
            when(zoneService.getZoneByIdAndUser(
                    testWarehouse.getId(),
                    testZone.getId(),
                    testUser
            )).thenReturn(testZone);

            mockMvc.perform(get("/warehouse/2/zone/3").with(user(testUser)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-zone"))
                    .andExpect(model().attributeExists(
                            "currentUser",
                            "warehouseId",
                            "warehouseName",
                            "zone",
                            "canManage"
                    ))
                    .andExpect(model().attribute(
                            "warehouseName", "Central Warehouse"
                    ))
                    .andExpect(model().attribute("canManage", true));

            verify(zoneService, times(1))
                    .getZoneByIdAndUser(
                            testWarehouse.getId(),
                            testZone.getId(),
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionOccursWhenAnExceptionIsThrown() throws Exception {
            when(zoneService.getZoneByIdAndUser(
                    testWarehouse.getId(), testZone.getId(), testUser
            )).thenThrow(new ResourceNotFoundException(
                    "Requested zone does not belong to the specified warehouse"
            ));

            mockMvc.perform(get("/warehouse/2/zone/3").with(user(testUser)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2"))
                    .andExpect(flash().attribute(
                            "error",
                            "Requested zone does not belong" +
                                    " to the specified warehouse"
                    ));
        }
    }

    @Nested
    class AddZoneTests {
        @Test
        void verifyIfRedirectionOccursAndSuccessFlashAttributeIsAdded() throws Exception {
            mockMvc.perform(post("/warehouse/2/add-zone")
                            .param("name", "Zone B")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2"))
                    .andExpect(flash().attribute(
                            "success",
                            "New zone added successfully.")
                    );

            verify(zoneService, times(1))
                    .addZone(
                            testWarehouse.getId(),
                            "Zone B",
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException("You are not permitted to perform" +
                    " modifications in this warehouse"))
                    .when(zoneService).addZone(
                            testWarehouse.getId(),
                            "Zone B",
                            testUser
                    );

            mockMvc.perform(post("/warehouse/2/add-zone")
                            .param("name", "Zone B")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2"))
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform" +
                                    " modifications in this warehouse"
                    ));
        }
    }

    @Nested
    class UpdateZoneTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/2/update-zone")
                            .param("warehouseId", "2")
                            .param("zoneId", "3")
                            .param("name", "New Name")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2"))
                    .andExpect(flash().attribute(
                            "success",
                            "Zone updated successfully"
                    ));

            verify(zoneService, times(1)).updateZone(
                    testWarehouse.getId(),
                    testZone.getId(),
                    "New Name",
                    testUser
            );
        }

        @Test
        void verifyIfErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceNotFoundException("Zone not found"))
                    .when(zoneService).updateZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            "New Name",
                            testUser
                    );

            mockMvc.perform(post("/warehouse/2/update-zone")
                            .param("warehouseId", "2")
                            .param("zoneId", "3")
                            .param("name", "New Name")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2"))
                    .andExpect(flash().attribute(
                            "error",
                            "Zone not found"
                    ));
        }
    }

    @Nested
    class DeleteZoneTests {
        @Test
        void verifyIfRedirectIonOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/2/delete-zone")
                            .param("zoneId", "3")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2"))
                    .andExpect(flash().attribute(
                            "success",
                            "Zone deleted successfully."
                    ));

            verify(zoneService, times(1))
                    .deleteZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceNotFoundException("Zone not found"))
                    .when(zoneService).deleteZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            testUser
                    );

            mockMvc.perform(post("/warehouse/2/delete-zone")
                            .param("zoneId", "3")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2"))
                    .andExpect(flash().attribute(
                            "error",
                            "Zone not found"
                    ));
        }
    }

    @Nested
    class AddItemToZoneTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/2/zone/3/add-item")
                            .param("name", "Item A")
                            .param("sku", "TEST-01")
                            .param("quantity", "5")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2/zone/3"))
                    .andExpect(flash().attribute(
                            "success",
                            "Item registered successfully."
                    ));

            verify(zoneService, times(1))
                    .addItemToZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            "Item A",
                            "TEST-01",
                            5L,
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform modifications" +
                            " in this warehouse"
            )).when(zoneService).addItemToZone(
                    testWarehouse.getId(),
                    testZone.getId(),
                    "Item A",
                    "TEST-01",
                    5L,
                    testUser
            );

            mockMvc.perform(post("/warehouse/2/zone/3/add-item")
                            .param("name", "Item A")
                            .param("sku", "TEST-01")
                            .param("quantity", "5")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2/zone/3"))
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform modifications" +
                                    " in this warehouse"
                    ));
        }
    }

    @Nested
    class UpdateItemInZoneTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/2/zone/3/update-item")
                            .param("itemId", "100")
                            .param("name", "New Name")
                            .param("sku", "TEST-02")
                            .param("quantity", "10")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2/zone/3"))
                    .andExpect(flash().attribute(
                            "success",
                            "Item updated successfully."
                    ));

            verify(zoneService, times(1)).updateItemInZone(
                    testWarehouse.getId(),
                    100L,
                    "New Name",
                    "TEST-02",
                    10L,
                    testUser
            );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceNotFoundException("Item does not exist"))
                    .when(zoneService).updateItemInZone(
                            testWarehouse.getId(),
                            100L,
                            "New Name",
                            "TEST-02",
                            10L,
                            testUser
                    );

            mockMvc.perform(post("/warehouse/2/zone/3/update-item")
                            .param("itemId", "100")
                            .param("name", "New Name")
                            .param("sku", "TEST-02")
                            .param("quantity", "10")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2/zone/3"))
                    .andExpect(flash().attribute(
                            "error",
                            "Item does not exist"
                    ));

        }
    }

    @Nested
    class RemoveItemFromZoneTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/2/zone/3/remove-item")
                            .param("itemId", "100")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2/zone/3"))
                    .andExpect(flash().attribute(
                            "success",
                            "Item removed successfully."
                    ));

            verify(zoneService, times(1))
                    .removeItemFromZone(
                            testWarehouse.getId(),
                            100L,
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException("You are not permitted to perform" +
                    " modifications in this warehouse"
            )).when(zoneService).removeItemFromZone(
                    testWarehouse.getId(),
                    100L,
                    testUser
            );

            mockMvc.perform(post("/warehouse/2/zone/3/remove-item")
                            .param("itemId", "100")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2/zone/3"))
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform modifications" +
                                    " in this warehouse"
                    ));
        }
    }
}

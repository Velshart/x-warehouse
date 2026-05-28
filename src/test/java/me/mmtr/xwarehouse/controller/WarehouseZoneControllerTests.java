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

    private User mockUser;
    private Warehouse mockWarehouse;
    private WarehouseZone mockZone;

    @BeforeEach
    public void setup() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockWarehouse = new Warehouse();
        mockWarehouse.setId(2L);
        mockWarehouse.setName("Central Warehouse");
        mockWarehouse.setOwner(mockUser);

        mockZone = new WarehouseZone("Zone A", mockWarehouse);
        mockZone.setId(3L);
    }

    @Nested
    class ViewZoneTests {
        @Test
        void verifyIfZoneViewIsReturnedCorrectly() throws Exception {
            when(zoneService.getZoneByIdAndUser(
                    mockWarehouse.getId(),
                    mockZone.getId(),
                    mockUser
            )).thenReturn(mockZone);

            mockMvc.perform(get("/warehouse/2/zone/3").with(user(mockUser)))
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
                            mockWarehouse.getId(),
                            mockZone.getId(),
                            mockUser
                    );
        }

        @Test
        void verifyIfRedirectionOccursWhenAnExceptionIsThrown() throws Exception {
            when(zoneService.getZoneByIdAndUser(
                    mockWarehouse.getId(), mockZone.getId(), mockUser
            )).thenThrow(new ResourceNotFoundException(
                    "Requested zone does not belong to the specified warehouse"
            ));

            mockMvc.perform(get("/warehouse/2/zone/3").with(user(mockUser)))
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
            doNothing().when(zoneService).addZone(
                    mockWarehouse.getId(),
                    "Zone B",
                    mockUser
            );

            mockMvc.perform(post("/warehouse/2/add-zone")
                            .param("name", "Zone B")
                            .with(user(mockUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2"))
                    .andExpect(flash().attribute(
                            "success",
                            "New zone added successfully.")
                    );

            verify(zoneService, times(1))
                    .addZone(
                            mockWarehouse.getId(),
                            "Zone B",
                            mockUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException("You are not permitted to perform" +
                    " modifications in this warehouse"))
                    .when(zoneService).addZone(
                            mockWarehouse.getId(),
                            "Zone B",
                            mockUser
                    );

            mockMvc.perform(post("/warehouse/2/add-zone")
                            .param("name", "Zone B")
                            .with(user(mockUser))
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
    class DeleteZoneTests {
        @Test
        void verifyIfRedirectIonOccursWithSuccessFlashAttribute() throws Exception {
            doNothing().when(zoneService).deleteZone(
                    mockWarehouse.getId(),
                    mockZone.getId(),
                    mockUser
            );

            mockMvc.perform(post("/warehouse/2/delete-zone")
                            .param("zoneId", "3")
                            .with(user(mockUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2"))
                    .andExpect(flash().attribute(
                            "success",
                            "Zone deleted successfully."
                    ));

            verify(zoneService, times(1))
                    .deleteZone(
                            mockWarehouse.getId(),
                            mockZone.getId(),
                            mockUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceNotFoundException("Zone not found"))
                    .when(zoneService).deleteZone(
                            mockWarehouse.getId(),
                            mockZone.getId(),
                            mockUser
                    );

            mockMvc.perform(post("/warehouse/2/delete-zone")
                            .param("zoneId", "3")
                            .with(user(mockUser))
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
            doNothing().when(zoneService).addItemToZone(
                    mockWarehouse.getId(),
                    mockZone.getId(),
                    "Item A",
                    "TEST-01",
                    5L,
                    mockUser
            );

            mockMvc.perform(post("/warehouse/2/zone/3/add-item")
                            .param("name", "Item A")
                            .param("sku", "TEST-01")
                            .param("quantity", "5")
                            .with(user(mockUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2/zone/3"))
                    .andExpect(flash().attribute(
                            "success",
                            "Item registered successfully."
                    ));

            verify(zoneService, times(1))
                    .addItemToZone(
                            mockWarehouse.getId(),
                            mockZone.getId(),
                            "Item A",
                            "TEST-01",
                            5L,
                            mockUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform modifications in this warehouse"
            )).when(zoneService).addItemToZone(
                    mockWarehouse.getId(),
                    mockZone.getId(),
                    "Item A",
                    "TEST-01",
                    5L,
                    mockUser
            );

            mockMvc.perform(post("/warehouse/2/zone/3/add-item")
                            .param("name", "Item A")
                            .param("sku", "TEST-01")
                            .param("quantity", "5")
                            .with(user(mockUser))
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
    class RemoveItemFromZoneTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            doNothing().when(zoneService).removeItemFromZone(
                    mockWarehouse.getId(), 100L, mockUser
            );

            mockMvc.perform(post("/warehouse/2/zone/3/remove-item")
                            .param("itemId", "100")
                            .with(user(mockUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/2/zone/3"))
                    .andExpect(flash().attribute(
                            "success",
                            "Item removed successfully."
                    ));

            verify(zoneService, times(1))
                    .removeItemFromZone(
                            mockWarehouse.getId(),
                            100L,
                            mockUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException("You are not permitted to perform" +
                    " modifications in this warehouse"
            )).when(zoneService).removeItemFromZone(
                    mockWarehouse.getId(),
                    100L,
                    mockUser
            );

            mockMvc.perform(post("/warehouse/2/zone/3/remove-item")
                            .param("itemId", "100")
                            .with(user(mockUser))
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

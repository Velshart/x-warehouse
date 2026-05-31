package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseItem;
import me.mmtr.xwarehouse.model.WarehouseZone;
import me.mmtr.xwarehouse.service.WarehouseItemService;
import me.mmtr.xwarehouse.service.WarehouseService;
import me.mmtr.xwarehouse.service.WarehouseZoneService;
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

@WebMvcTest(WarehouseZoneController.class)
public class WarehouseZoneControllerTests {

    @MockitoBean
    private WarehouseZoneService zoneService;

    @MockitoBean
    private WarehouseService warehouseService;

    @MockitoBean
    private WarehouseItemService itemService;

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
        WarehouseItem testItem;

        @BeforeEach
        public void setup() {
            testItem = new WarehouseItem(
                    "Test Item",
                    "TEST-01",
                    5L,
                    testZone
            );
            testItem.setId(4L);
        }

        @Test
        void verifyIfZoneViewIsReturnedCorrectly() throws Exception {
            when(zoneService.getZoneByIdAndUser(
                    testWarehouse.getId(),
                    testZone.getId(),
                    testUser
            )).thenReturn(testZone);

            when(itemService.getItemsByWarehouseIdAndUser(
                    testWarehouse.getId(),
                    testZone.getId(),
                    "Test",
                    testUser
            )).thenReturn(List.of(testItem));

            when(zoneService.getZonesByWarehouseId(
                    testWarehouse.getId(),
                    null,
                    testUser
            )).thenReturn(List.of(testZone));

            mockMvc.perform(get("/warehouse/" +
                            testWarehouse.getId() + "/zone/" +
                            testZone.getId())
                            .param("search", "Test")
                            .with(user(testUser)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-zone"))
                    .andExpect(model().attribute("currentUser", testUser))
                    .andExpect(model().attribute(
                            "warehouseId",
                            testWarehouse.getId()
                    ))
                    .andExpect(model().attribute("warehouseName",
                            testWarehouse.getName()
                    ))
                    .andExpect(model().attribute("zone", testZone))
                    .andExpect(model().attribute(
                            "warehouseName", testWarehouse.getName()
                    ))
                    .andExpect(model().attribute(
                            "items", List.of(testItem)
                    ))
                    .andExpect(model().attribute(
                            "allZones", List.of(testZone)
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

            mockMvc.perform(get("/warehouse/" +
                            testWarehouse.getId() +
                            "/zone/" + testZone.getId())
                            .with(user(testUser)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId()
                    ))
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
            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/add-zone")
                            .param("name", "Zone B")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId()))
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

            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/add-zone")
                            .param("name", "Zone B")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId()))
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
            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/update-zone")
                            .param("warehouseId", String.valueOf(testWarehouse.getId()))
                            .param("zoneId", String.valueOf(testZone.getId()))
                            .param("name", "New Name")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId()))
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

            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/update-zone")
                            .param("warehouseId", String.valueOf(testWarehouse.getId()))
                            .param("zoneId", String.valueOf(testZone.getId()))
                            .param("name", "New Name")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId()))
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
            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/delete-zone")
                            .param("zoneId", String.valueOf(testZone.getId()))
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId()))
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

            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/delete-zone")
                            .param("zoneId", String.valueOf(testZone.getId()))
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId()))
                    .andExpect(flash().attribute(
                            "error",
                            "Zone not found"
                    ));
        }
    }
}
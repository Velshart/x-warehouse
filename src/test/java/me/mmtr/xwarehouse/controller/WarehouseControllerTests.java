package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseZone;
import me.mmtr.xwarehouse.model.enums.WarehouseRole;
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

@WebMvcTest(WarehouseController.class)
public class WarehouseControllerTests {

    @MockitoBean
    private WarehouseService warehouseService;

    @MockitoBean
    private WarehouseZoneService zoneService;

    private User currentUser;
    private User ownerUser;
    private Warehouse warehouse;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("currentUser");

        ownerUser = new User();
        ownerUser.setId(2L);
        ownerUser.setUsername("ownerUser");

        warehouse = new Warehouse("Test Warehouse", ownerUser);
        warehouse.setId(3L);
    }

    @Nested
    class WarehouseViewTests {
        private WarehouseZone testZone;

        @BeforeEach
        public void setup() {
            testZone = new WarehouseZone("Zone A", warehouse);
            testZone.setId(4L);
        }

        @Test
        void verifyIfWarehouseViewIsReturnedCorrectly() throws Exception {
            when(warehouseService.getWarehouseByIdAndUser(
                    warehouse.getId(), ownerUser)).thenReturn(warehouse);

            when(zoneService.getZonesByWarehouseId(
                    warehouse.getId(),
                    "Zone A",
                    ownerUser
            )).thenReturn(List.of(testZone));

            mockMvc.perform(get("/warehouse/" + warehouse.getId())
                            .param("search", testZone.getName())
                            .with(user(ownerUser)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-warehouse"))
                    .andExpect(model().attribute("currentUser", ownerUser))
                    .andExpect(model().attribute("isOwner", true))
                    .andExpect(model().attribute("isAdmin", false))
                    .andExpect(model().attribute("canManage", true))
                    .andExpect(model().attribute("zones", List.of(testZone)))
                    .andExpect(model().attribute("warehouse", warehouse));
        }

        @Test
        void shouldRedirectToDashboardWithErrorFlashAttributeWhenExceptionIsThrown() throws Exception {

            when(warehouseService.getWarehouseByIdAndUser(warehouse.getId(), currentUser))
                    .thenThrow(new ResourceAccessException("Access denied"));

            mockMvc.perform(get("/warehouse/" + warehouse.getId())
                            .with(user(currentUser)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "error",
                            "Access denied"
                    ));
        }
    }

    @Nested
    class CreateWarehouseTests {
        @Test
        void verifyIfWarehouseIsCorrectlyCreatedAndRedirectionOccurs() throws Exception {
            mockMvc.perform(post("/warehouse/create")
                            .with(user(currentUser)).param(
                                    "name", "Warehouse 1")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "success",
                            "Warehouse created successfully"
                    ));

            verify(warehouseService, times(1))
                    .createWarehouse("Warehouse 1", currentUser);
        }

        @Test
        void verifyIfErrorRedirectionOccursWithErrorAttributeWhenExceptionIsThrown() throws Exception {
            when(warehouseService.createWarehouse("  ", currentUser))
                    .thenThrow(new BusinessValidationException(
                            "Name cannot be empty"
                    ));

            mockMvc.perform(post("/warehouse/create")
                            .with(user(currentUser))
                            .with(csrf())
                            .param("name", "  "))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "error",
                            "Name cannot be empty")
                    );
        }
    }

    @Nested
    class UpdateWarehouseTests {
        @Test
        void verifyIfWarehouseIsCorrectlyUpdatedAndRedirectionOccurs() throws Exception {
            mockMvc.perform(post("/warehouse/update")
                            .param("warehouseId", String.valueOf(warehouse.getId()))
                            .param("name", "New Name")
                            .with(user(currentUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "success",
                            "Warehouse updated successfully")
                    );

            verify(warehouseService, times(1))
                    .updateWarehouse(
                            warehouse.getId(),
                            "New Name",
                            currentUser
                    );
        }

        @Test
        void verifyIfErrorParamIsSetWhenExceptionIsThrown() throws Exception {
            doThrow(new ResourceNotFoundException("Warehouse not found"))
                    .when(warehouseService).updateWarehouse(
                            warehouse.getId(), "New Name", currentUser
                    );

            mockMvc.perform(post("/warehouse/update")
                            .param("warehouseId", String.valueOf(warehouse.getId()))
                            .param("name", "New Name")
                            .with(user(currentUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "error",
                            "Warehouse not found")
                    );
        }
    }

    @Nested
    class DeleteWarehouseTests {
        @Test
        void verifyIfWarehouseIsCorrectlyDeletedAndRedirectionOccurs() throws Exception {
            mockMvc.perform(post("/warehouse/delete")
                            .param("warehouseId", String.valueOf(warehouse.getId()))
                            .with(user(currentUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "success",
                            "Warehouse deleted successfully")
                    );

            verify(
                    warehouseService,
                    times(1)
            ).deleteWarehouse(warehouse.getId(), currentUser);
        }

        @Test
        void verifyIfErrorParamIsSetWhenExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException(
                    "Current user is not the owner of this warehouse"
            )).when(warehouseService).deleteWarehouse(warehouse.getId(), currentUser);

            mockMvc.perform(post("/warehouse/delete")
                            .param(
                                    "warehouseId",
                                    String.valueOf(warehouse.getId())
                            )
                            .with(user(currentUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "error",
                            "Current user is not the owner of this warehouse")
                    );
        }
    }

    @Nested
    class AddMemberTests {
        @Test
        void verifyIfMemberIsAddedSuccessfullyAndRedirectionOccurs() throws Exception {
            mockMvc.perform(post("/warehouse/" + warehouse.getId() + "/add-member")
                            .with(user(currentUser))
                            .with(csrf())
                            .param("username", "newUser")
                            .param("role", "ADMIN"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + warehouse.getId()))
                    .andExpect(flash().attribute(
                            "success",
                            "New member added successfully")
                    );

            verify(
                    warehouseService,
                    times(1)
            ).addMember(
                    warehouse.getId(),
                    "newUser",
                    WarehouseRole.ADMIN, currentUser
            );
        }

        @Test
        void shouldRedirectWithErrorAttributeWhenExceptionWasThrown() throws Exception {
            doThrow(new BusinessValidationException(
                    "User is already a member of this warehouse"
            )).when(warehouseService).addMember(
                    anyLong(),
                    anyString(),
                    any(WarehouseRole.class),
                    any(User.class)
            );

            mockMvc.perform(post("/warehouse/" + warehouse.getId() + "/add-member")
                            .with(user(currentUser))
                            .with(csrf())
                            .param("username", "existingUser")
                            .param("role", "VIEWER"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + warehouse.getId()))
                    .andExpect(flash().attribute(
                            "error",
                            "User is already a member of this warehouse")
                    );
        }
    }

    @Nested
    class RemoveMemberTests {
        @Test
        void verifyIfMemberIsRemovedSuccessfullyAndRedirectionOccurs() throws Exception {
            mockMvc.perform(post("/warehouse/" + warehouse.getId() + "/remove-member")
                            .with(user(ownerUser))
                            .with(csrf())
                            .param("userId", String.valueOf(ownerUser.getId())))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + warehouse.getId()))
                    .andExpect(flash().attribute(
                            "success",
                            "Member removed successfully"
                    ));

            verify(warehouseService, times(1))
                    .removeMember(warehouse.getId(), ownerUser.getId(), ownerUser);
        }

        @Test
        void shouldRedirectWithErrorAttributeWhenExceptionWasThrown() throws Exception {
            doThrow(new BusinessValidationException(
                    "An owner cannot be removed from members list")
            ).when(warehouseService).removeMember(
                    warehouse.getId(),
                    currentUser.getId(),
                    ownerUser
            );

            mockMvc.perform(post("/warehouse/" + warehouse.getId() + "/remove-member")
                            .with(user(ownerUser))
                            .with(csrf())
                            .param("userId", String.valueOf(currentUser.getId())))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + warehouse.getId()))
                    .andExpect(flash().attribute(
                            "error",
                            "An owner cannot be removed from members list"
                    ));
        }
    }
}
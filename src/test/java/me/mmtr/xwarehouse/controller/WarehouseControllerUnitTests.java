package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.enums.WarehouseRole;
import me.mmtr.xwarehouse.service.WarehouseService;
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

@WebMvcTest(WarehouseController.class)
public class WarehouseControllerUnitTests {

    @MockitoBean
    private WarehouseService warehouseService;

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
        @Test
        void verifyIfWarehouseViewIsReturnedCorrectly() throws Exception {
            when(warehouseService.getWarehouseByIdAndUser(
                    3L, ownerUser)).thenReturn(warehouse);

            mockMvc.perform(get("/warehouse/3")
                            .with(user(ownerUser)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-warehouse"))
                    .andExpect(model().attribute("currentUser", ownerUser))
                    .andExpect(model().attribute("isOwner", true))
                    .andExpect(model().attribute("isAdmin", false))
                    .andExpect(model().attribute("canManage", true))
                    .andExpect(model().attribute("warehouse", warehouse));
        }

        @Test
        void shouldRedirectToDashboardWithErrorFlashAttributeWhenExceptionisThrown() throws Exception {

            when(warehouseService.getWarehouseByIdAndUser(3L, currentUser))
                    .thenThrow(new ResourceAccessException("Access denied"));

            mockMvc.perform(get("/warehouse/3")
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
    class AddMemberTests {
        @Test
        void verifyIfMemberIsAddedSuccessfullyAndRedirectionOccurs() throws Exception {
            mockMvc.perform(post("/warehouse/3/add-member")
                            .with(user(currentUser))
                            .with(csrf())
                            .param("username", "newUser")
                            .param("role", "ADMIN"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/3"))
                    .andExpect(flash().attribute(
                            "success",
                            "New member added successfully")
                    );

            verify(
                    warehouseService,
                    times(1)
            ).addMember(
                    3L,
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

            mockMvc.perform(post("/warehouse/3/add-member")
                            .with(user(currentUser))
                            .with(csrf())
                            .param("username", "existingUser")
                            .param("role", "VIEWER"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/3"))
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
            mockMvc.perform(post("/warehouse/3/remove-member")
                            .with(user(ownerUser))
                            .with(csrf())
                            .param("userId", "2"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/3"))
                    .andExpect(flash().attribute(
                            "success",
                            "Member removed successfully"
                    ));

            verify(warehouseService, times(1))
                    .removeMember(3L, 2L, ownerUser);
        }

        @Test
        void shouldRedirectWithErrorAttributeWhenExceptionWasThrown() throws Exception {
            doThrow(new BusinessValidationException(
                    "An owner cannot be removed from members list")
            ).when(warehouseService).removeMember(
                    3L,
                    1L,
                    ownerUser
            );

            mockMvc.perform(post("/warehouse/3/remove-member")
                            .with(user(ownerUser))
                            .with(csrf())
                            .param("userId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/3"))
                    .andExpect(flash().attribute(
                            "error",
                            "An owner cannot be removed from members list"
                    ));
        }
    }
}


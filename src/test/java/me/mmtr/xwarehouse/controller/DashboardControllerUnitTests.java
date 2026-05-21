package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
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

@WebMvcTest(DashboardController.class)
public class DashboardControllerUnitTests {

    @MockitoBean
    private WarehouseService warehouseService;

    @Autowired
    private MockMvc mockMvc;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
    }

    @Nested
    class DashboardViewTests {
        @Test
        void verifyIfViewWithWarehousesIsReturnedCorrectly() throws Exception {
            List<Warehouse> warehouses = List.of(
                    new Warehouse("Warehouse 1", testUser),
                    new Warehouse("Warehouse 2", testUser)
            );

            when(warehouseService.getAvailableWarehouses(testUser.getId()))
                    .thenReturn(warehouses);

            mockMvc.perform(get("/dashboard")
                            .with(user(testUser)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("warehouses", warehouses))
                    .andExpect(model().attribute("currentUser", testUser));

            verify(warehouseService, times(1))
                    .getAvailableWarehouses(testUser.getId());
        }
    }

    @Nested
    class CreateWarehouseTests {
        @Test
        void verifyIfWarehouseIsCorrectlyCreatedAndRedirectionOccurs() throws Exception {
            mockMvc.perform(post("/dashboard/warehouse/create")
                            .with(user(testUser)).param(
                                    "name", " Warehouse 1 ")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "success",
                            "Warehouse created successfully"
                    ));

            verify(warehouseService, times(1))
                    .createWarehouse("Warehouse 1", testUser);
        }

        @Test
        void verifyIfErrorOccursWhenNameIsEmpty() throws Exception {
            mockMvc.perform(post("/dashboard/warehouse/create")
                            .with(user(testUser))
                            .with(csrf())
                            .param("name", "    "))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "error",
                            "Name cannot be empty")
                    );
        }

        @Test
        void verifyIfErrorParamIsSetWhenExceptionWasThrown() throws Exception {
            doThrow(new RuntimeException("Test exception")).when(warehouseService)
                    .createWarehouse(anyString(), any(User.class));

            mockMvc.perform(post("/dashboard/warehouse/create")
                            .param("name", "Warehouse 1")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "error",
                            "Test exception")
                    );
        }
    }

    @Nested
    class DeleteWarehouseTests {
        @Test
        void verifyIfWarehouseIsCorrectlyDeletedAndRedirectionOccurs() throws Exception {
            mockMvc.perform(post("/dashboard/warehouse/delete")
                            .param("warehouseId", "1")
                            .with(user(testUser))
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
            ).deleteWarehouse(1L, testUser);
        }

        @Test
        void verifyIfErrorParamIsSetWhenExceptionIsThrown() throws Exception {
            doThrow(new RuntimeException("Test exception"))
                    .when(warehouseService).deleteWarehouse(1L, testUser);

            mockMvc.perform(post("/dashboard/warehouse/delete")
                            .param("warehouseId", "1")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"))
                    .andExpect(flash().attribute(
                            "error",
                            "Test exception")
                    );
        }
    }
}

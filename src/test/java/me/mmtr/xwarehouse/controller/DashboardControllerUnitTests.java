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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}

package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.model.*;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderStatus;
import me.mmtr.xwarehouse.service.WarehouseContractorService;
import me.mmtr.xwarehouse.service.WarehouseItemService;
import me.mmtr.xwarehouse.service.WarehouseOrderService;
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

@WebMvcTest(WarehouseOrderController.class)
public class WarehouseOrderControllerTests {

    @MockitoBean
    private WarehouseOrderService warehouseOrderService;

    @MockitoBean
    private WarehouseItemService warehouseItemService;

    @MockitoBean
    private WarehouseContractorService warehouseContractorService;

    private User testUser;
    private Warehouse testWarehouse;
    private WarehouseOrder testOrder;
    private WarehouseContractor testContractor;

    private WarehouseItem testItem;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testWarehouse = new Warehouse();
        testWarehouse.setId(2L);
        testWarehouse.setOwner(testUser);

        testOrder = new WarehouseOrder();
        testOrder.setId(3L);
        testOrder.setWarehouse(testWarehouse);
        testOrder.setWarehouseOrderStatus(WarehouseOrderStatus.WAITING);

        testContractor = new WarehouseContractor();
        testContractor.setId(4L);
        testContractor.setWarehouse(testWarehouse);

        testItem = new WarehouseItem();
        testItem.setId(5L);
    }

    @Nested
    class GetContractorOrdersTests {
        @Test
        void verifyIfManageOrdersViewAndModelAreCorrectlyReturned() throws Exception {
            when(warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    testUser
            )).thenReturn(List.of(testOrder));

            when(warehouseItemService.getItemsByWarehouseId(
                    testWarehouse.getId(),
                    testUser
            )).thenReturn(List.of(testItem));

            when(warehouseContractorService.getContractorById(
                    testWarehouse.getId(), testContractor.getId(), testUser
            )).thenReturn(testContractor);

            mockMvc.perform(get(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractor-orders/" +
                                    testContractor.getId())
                            .with(user(testUser)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-orders"))
                    .andExpect(model().attribute(
                            "orderList",
                            List.of(testOrder))
                    )
                    .andExpect(model().attribute(
                            "itemList",
                            List.of(testItem))
                    )
                    .andExpect(model().attribute(
                            "contractor",
                            testContractor)
                    )
                    .andExpect(model().attribute(
                            "warehouseId",
                            testWarehouse.getId())
                    )
                    .andExpect(model().attribute("currentUser", testUser))
                    .andExpect(model().attribute(
                            "statuses",
                            WarehouseOrderStatus.values()
                    ));
        }

        @Test
        void verifyIfRedirectionWithErrorAttributeOccursWhenExceptionWasThrown() throws Exception {
            when(warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    testUser
            )).thenThrow(new ResourceAccessException(
                    "You are not permitted to perform " +
                            "modifications in this warehouse"
            ));

            mockMvc.perform(get(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractor-orders/" +
                                    testContractor.getId())
                            .with(user(testUser)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractors"
                    ))
                    .andExpect(flash().attribute("error",
                            "You are not permitted to perform " +
                                    "modifications in this warehouse"
                    ));
        }
    }

    @Nested
    class CreateOrderTests {
        @Test
        void verifyIfOrderIsCreatedSuccessfully() throws Exception {
            mockMvc.perform(post(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractor-orders/" +
                                    testContractor.getId() +
                                    "/create")
                            .with(user(testUser)).with(csrf())
                            .param("warehouseItemId", "5")
                            .param("quantity", "10"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-orders/" + testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "success",
                            "Order created successfully"
                    ));

            verify(
                    warehouseOrderService,
                    times(1))
                    .createOrder(
                            eq(testWarehouse.getId()),
                            eq(testContractor.getId()),
                            anyList(),
                            eq(testUser)
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorAttributeOccursWhenExceptionWasThrown() throws Exception {
            doThrow(new ResourceAccessException("You are not permitted to perform " +
                    "modifications in this warehouse"))
                    .when(warehouseOrderService).createOrder(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            List.of(),
                            testUser
                    );

            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() +
                            "/contractor-orders/" +
                            testContractor.getId() +
                            "/create")
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractor-orders/" +
                                    testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform " +
                                    "modifications in this warehouse"
                    ));
        }
    }

    @Nested
    class ChangeOrderStatus {
        @Test
        void verifyIfOrderStatusIsChangedSuccessfully() throws Exception {
            mockMvc.perform(post(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-orders/" + testContractor.getId() +
                                    "/order/" + testOrder.getId() + "/change-status")
                            .with(user(testUser))
                            .with(csrf())
                            .param("newStatus", "IN_PROGRESS"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-orders/" +
                                    testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "success",
                            "Order status changed successfully"

                    ));

            verify(warehouseOrderService, times(1))
                    .changeOrderStatus(
                            testWarehouse.getId(),
                            testOrder.getId(),
                            WarehouseOrderStatus.IN_PROGRESS,
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorAttributeOccursWhenExceptionWasThrown() throws Exception {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform " +
                            "modifications in this warehouse"
            )).when(warehouseOrderService).changeOrderStatus(
                    testWarehouse.getId(),
                    testOrder.getId(),
                    WarehouseOrderStatus.IN_PROGRESS,
                    testUser
            );

            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() +
                            "/contractor-orders/" + testContractor.getId() +
                            "/order/" + testOrder.getId() + "/change-status")
                            .with(user(testUser))
                            .with(csrf())
                            .param("newStatus", "IN_PROGRESS"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-orders/" +
                                    testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform " +
                                    "modifications in this warehouse"
                    ));
        }
    }

    @Nested
    class DeleteOrderTests {
        @Test
        void verifyIfOrderIsDeletedSuccessfully() throws Exception {
            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() + "/contractor-orders/" +
                            testContractor.getId() + "/order/" + testOrder.getId() +
                            "/delete").with(user(testUser)).with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-orders/" + testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "success",
                            "Order deleted successfully"
                    ));

            verify(warehouseOrderService, times(1))
                    .deleteOrder(
                            testWarehouse.getId(),
                            testOrder.getId(),
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorAttributeOccursWhenExceptionWasThrown() throws Exception {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform " +
                            "modifications in this warehouse"
            )).when(warehouseOrderService).deleteOrder(
                    testWarehouse.getId(),
                    testOrder.getId(),
                    testUser
            );

            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() + "/contractor-orders/" +
                            testContractor.getId() + "/order/" +
                            testOrder.getId() + "/delete")
                            .with(user(testUser)).with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-orders/" + testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform " +
                                    "modifications in this warehouse"
                    ));
        }
    }
}

package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.model.*;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderStatus;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderType;
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
    private WarehouseOrder testInboundOrder;
    private WarehouseOrder testOutboundOrder;
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

        testInboundOrder = new WarehouseOrder();
        testInboundOrder.setId(3L);
        testInboundOrder.setWarehouse(testWarehouse);
        testInboundOrder.setWarehouseOrderStatus(WarehouseOrderStatus.WAITING);
        testInboundOrder.setWarehouseOrderType(WarehouseOrderType.INBOUND);

        testOutboundOrder = new WarehouseOrder();
        testOutboundOrder.setId(4L);
        testOutboundOrder.setWarehouse(testWarehouse);
        testOutboundOrder.setWarehouseOrderStatus(WarehouseOrderStatus.WAITING);
        testOutboundOrder.setWarehouseOrderType(WarehouseOrderType.INBOUND);

        testContractor = new WarehouseContractor();
        testContractor.setId(5L);
        testContractor.setWarehouse(testWarehouse);

        testItem = new WarehouseItem();
        testItem.setId(6L);
    }

    @Nested
    class GetContractorOrdersTests {
        @Test
        void verifyIfManageOrdersViewAndModelAreCorrectlyReturned() throws Exception {
            when(warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    testUser,
                    false
            )).thenReturn(List.of(testInboundOrder));

            when(warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    testUser,
                    true
            )).thenReturn(List.of(testOutboundOrder));

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
                                    "/contractor-outbound-orders/" +
                                    testContractor.getId())
                            .with(user(testUser)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-outbound-orders"))
                    .andExpect(model().attribute(
                            "orderList",
                            List.of(testOutboundOrder))
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

            mockMvc.perform(get(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractor-inbound-orders/" +
                                    testContractor.getId())
                            .with(user(testUser)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("manage-inbound-orders"))
                    .andExpect(model().attribute(
                            "orderList",
                            List.of(testInboundOrder))
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
                    testUser,
                    false
            )).thenThrow(new ResourceAccessException(
                    "You are not permitted to perform " +
                            "modifications in this warehouse"
            ));

            when(warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    testUser,
                    true
            )).thenThrow(new ResourceAccessException(
                    "You are not permitted to perform " +
                            "modifications in this warehouse"
            ));

            mockMvc.perform(get(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractor-inbound-orders/" +
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

            mockMvc.perform(get(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractor-outbound-orders/" +
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
                            .param("quantity", "10")
                            .param("outbound", "false"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-inbound-orders/" + testContractor.getId())
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
                            eq(false),
                            eq(null),
                            eq(testUser)
                    );

            mockMvc.perform(post(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractor-orders/" +
                                    testContractor.getId() +
                                    "/create")
                            .with(user(testUser)).with(csrf())
                            .param("warehouseItemId", "5")
                            .param("quantity", "10")
                            .param("outbound", "true"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-outbound-orders/" + testContractor.getId())
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
                            eq(true),
                            eq(null),
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
                            false,
                            null,
                            testUser
                    );

            doThrow(new ResourceAccessException("You are not permitted to perform " +
                    "modifications in this warehouse"))
                    .when(warehouseOrderService).createOrder(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            List.of(),
                            true,
                            null,
                            testUser
                    );

            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() +
                            "/contractor-orders/" +
                            testContractor.getId() +
                            "/create")
                            .with(user(testUser))
                            .with(csrf())
                            .param("outbound", "false"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractor-inbound-orders/" +
                                    testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform " +
                                    "modifications in this warehouse"
                    ));

            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() +
                            "/contractor-orders/" +
                            testContractor.getId() +
                            "/create")
                            .with(user(testUser))
                            .with(csrf())
                            .param("outbound", "true"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" +
                                    testWarehouse.getId() +
                                    "/contractor-outbound-orders/" +
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
                                    "/order/" + testInboundOrder.getId() + "/change-status")
                            .with(user(testUser))
                            .with(csrf())
                            .param("newStatus", "IN_PROGRESS")
                            .param("outbound", "false"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-inbound-orders/" +
                                    testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "success",
                            "Order status changed successfully"

                    ));

            verify(warehouseOrderService, times(1))
                    .changeOrderStatus(
                            testWarehouse.getId(),
                            testInboundOrder.getId(),
                            WarehouseOrderStatus.IN_PROGRESS,
                            testUser
                    );

            mockMvc.perform(post(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-orders/" + testContractor.getId() +
                                    "/order/" + testOutboundOrder.getId() + "/change-status")
                            .with(user(testUser))
                            .with(csrf())
                            .param("newStatus", "IN_PROGRESS")
                            .param("outbound", "true"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-outbound-orders/" +
                                    testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "success",
                            "Order status changed successfully"

                    ));

            verify(warehouseOrderService, times(1))
                    .changeOrderStatus(
                            testWarehouse.getId(),
                            testOutboundOrder.getId(),
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
                    testInboundOrder.getId(),
                    WarehouseOrderStatus.IN_PROGRESS,
                    testUser
            );

            doThrow(new ResourceAccessException(
                    "You are not permitted to perform " +
                            "modifications in this warehouse"
            )).when(warehouseOrderService).changeOrderStatus(
                    testWarehouse.getId(),
                    testOutboundOrder.getId(),
                    WarehouseOrderStatus.IN_PROGRESS,
                    testUser
            );

            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() +
                            "/contractor-orders/" + testContractor.getId() +
                            "/order/" + testInboundOrder.getId() + "/change-status")
                            .with(user(testUser))
                            .with(csrf())
                            .param("newStatus", "IN_PROGRESS")
                            .param("outbound", "false"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-inbound-orders/" +
                                    testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform " +
                                    "modifications in this warehouse"
                    ));

            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() +
                            "/contractor-orders/" + testContractor.getId() +
                            "/order/" + testOutboundOrder.getId() + "/change-status")
                            .with(user(testUser))
                            .with(csrf())
                            .param("newStatus", "IN_PROGRESS")
                            .param("outbound", "true"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-outbound-orders/" +
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
                            testContractor.getId() + "/order/" + testInboundOrder.getId() +
                            "/delete").with(user(testUser)).with(csrf())
                            .param("outbound", "false"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-inbound-orders/" + testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "success",
                            "Order deleted successfully"
                    ));

            verify(warehouseOrderService, times(1))
                    .deleteOrder(
                            testWarehouse.getId(),
                            testInboundOrder.getId(),
                            testUser
                    );

            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() + "/contractor-orders/" +
                            testContractor.getId() + "/order/" + testOutboundOrder.getId() +
                            "/delete").with(user(testUser)).with(csrf())
                            .param("outbound", "true"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-outbound-orders/" + testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "success",
                            "Order deleted successfully"
                    ));
        }

        @Test
        void verifyIfRedirectionWithErrorAttributeOccursWhenExceptionWasThrown() throws Exception {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform " +
                            "modifications in this warehouse"
            )).when(warehouseOrderService).deleteOrder(
                    testWarehouse.getId(),
                    testInboundOrder.getId(),
                    testUser
            );

            doThrow(new ResourceAccessException(
                    "You are not permitted to perform " +
                            "modifications in this warehouse"
            )).when(warehouseOrderService).deleteOrder(
                    testWarehouse.getId(),
                    testOutboundOrder.getId(),
                    testUser
            );

            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() + "/contractor-orders/" +
                            testContractor.getId() + "/order/" +
                            testInboundOrder.getId() + "/delete")
                            .with(user(testUser)).with(csrf())
                            .param("outbound", "false"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-inbound-orders/" + testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform " +
                                    "modifications in this warehouse"
                    ));

            mockMvc.perform(post("/warehouse/" +
                            testWarehouse.getId() + "/contractor-orders/" +
                            testContractor.getId() + "/order/" +
                            testOutboundOrder.getId() + "/delete")
                            .with(user(testUser)).with(csrf())
                            .param("outbound", "true"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(
                            "/warehouse/" + testWarehouse.getId() +
                                    "/contractor-outbound-orders/" + testContractor.getId())
                    )
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform " +
                                    "modifications in this warehouse"
                    ));
        }
    }
}

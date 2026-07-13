package me.mmtr.xwarehouse.service;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.*;
import me.mmtr.xwarehouse.model.dto.OrderItemRequest;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderStatus;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderType;
import me.mmtr.xwarehouse.repository.WarehouseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarehouseOrderServiceTests {
    @Captor
    private ArgumentCaptor<WarehouseOrder> warehouseOrderCaptor;

    @Mock
    private WarehouseOrderRepository warehouseOrderRepository;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private WarehouseContractorService warehouseContractorService;

    @Mock
    private WarehouseItemService warehouseItemService;

    @InjectMocks
    private WarehouseOrderService warehouseOrderService;

    private Warehouse testWarehouse;
    private User currentUser;
    private WarehouseOrder testOrderInbound;
    private WarehouseOrder testOrderOutbound;
    private WarehouseContractor testContractor;

    @BeforeEach
    public void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testUser");

        testWarehouse = new Warehouse("Warehouse 1", currentUser);
        testWarehouse.setId(2L);
        testWarehouse.setOwner(currentUser);

        testContractor = new WarehouseContractor();
        testContractor.setId(3L);
        testContractor.setWarehouse(testWarehouse);

        testOrderInbound = new WarehouseOrder();
        testOrderInbound.setId(4L);
        testOrderInbound.setWarehouse(testWarehouse);
        testOrderInbound.setWarehouseContractor(testContractor);
        testOrderInbound.setWarehouseOrderStatus(WarehouseOrderStatus.WAITING);
        testOrderInbound.setWarehouseOrderType(WarehouseOrderType.INBOUND);

        testOrderOutbound = new WarehouseOrder();
        testOrderOutbound.setId(5L);
        testOrderOutbound.setWarehouse(testWarehouse);
        testOrderOutbound.setWarehouseContractor(testContractor);
        testOrderOutbound.setWarehouseOrderStatus(WarehouseOrderStatus.WAITING);
        testOrderOutbound.setWarehouseOrderType(WarehouseOrderType.OUTBOUND);
    }

    @Nested
    class GetOrderByIdTests {
        @Test
        void verifyIfOrderIsCorrectlyReturned() {
            when(warehouseOrderRepository.findByIdAndWarehouseId(
                    testOrderInbound.getId(),
                    testWarehouse.getId())
            ).thenReturn(Optional.of(testOrderInbound));

            WarehouseOrder order = warehouseOrderService.getOrderById(
                    testOrderInbound.getId(), testWarehouse.getId(), currentUser
            );

            assertNotNull(order);
            assertEquals(testOrderInbound.getId(), order.getId());
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoReadAccess() {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateReadAccess(testWarehouse.getId(), currentUser);

            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseOrderService.getOrderById(
                                    testOrderInbound.getId(),
                                    testWarehouse.getId(),
                                    currentUser
                            ));

            assertEquals("You are not permitted to perform" +
                    " modifications in this warehouse", ex.getMessage());
        }

        @Test
        void verifyIfExceptionIsThrownWhenOrderNotFound() {
            when(warehouseOrderRepository.findByIdAndWarehouseId(
                    testOrderInbound.getId(),
                    testWarehouse.getId())
            ).thenReturn(Optional.empty());

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            warehouseOrderService.getOrderById(
                                    testOrderInbound.getId(),
                                    testWarehouse.getId(),
                                    currentUser
                            ));

            assertEquals(
                    "Order with provided id not found",
                    ex.getMessage()
            );
        }
    }

    @Nested
    class GetOrdersByWarehouseIdAndContractorIdTests {
        @Test
        void verifyIfOrdersAreCorrectlyReturned() {
            when(warehouseOrderRepository
                    .findAllByWarehouseIdAndWarehouseContractorIdAndWarehouseOrderType(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            WarehouseOrderType.INBOUND
                    )).thenReturn(List.of(testOrderInbound));

            when(warehouseOrderRepository
                    .findAllByWarehouseIdAndWarehouseContractorIdAndWarehouseOrderType(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            WarehouseOrderType.OUTBOUND
                    )).thenReturn(List.of(testOrderOutbound));

            List<WarehouseOrder> inboundOrders =
                    warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            currentUser,
                            false
                    );

            assertEquals(1, inboundOrders.size());
            assertEquals(testOrderInbound.getId(), inboundOrders.getFirst().getId());

            List<WarehouseOrder> outboundOrders =
                    warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            currentUser,
                            true
                    );

            assertEquals(1, outboundOrders.size());
            assertEquals(
                    testOrderOutbound.getId(),
                    outboundOrders.getFirst().getId()
            );
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoReadAccess() {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateReadAccess(testWarehouse.getId(), currentUser);

            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                                    testWarehouse.getId(),
                                    testContractor.getId(),
                                    currentUser,
                                    false
                            ));

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex.getMessage());
        }
    }

    @Nested
    class CreateOrderTests {
        private WarehouseItem warehouseItem1;
        private OrderItemRequest orderItemRequest1;

        private WarehouseItem warehouseItem2;
        private OrderItemRequest orderItemRequest2;

        private OrderItemRequest duplicateOrderItemRequest;

        @BeforeEach
        void setUp() {
            warehouseItem1 = new WarehouseItem();
            warehouseItem1.setId(6L);

            warehouseItem2 = new WarehouseItem();
            warehouseItem2.setId(8L);

            orderItemRequest1 = new OrderItemRequest();
            orderItemRequest1.setWarehouseItemId(warehouseItem1.getId());
            orderItemRequest1.setQuantity(5L);

            orderItemRequest2 = new OrderItemRequest();
            orderItemRequest2.setWarehouseItemId(warehouseItem2.getId());
            orderItemRequest2.setQuantity(3L);

            duplicateOrderItemRequest = new OrderItemRequest();
            duplicateOrderItemRequest.setWarehouseItemId(warehouseItem1.getId());
            duplicateOrderItemRequest.setQuantity(2L);
        }

        @Test
        void verifyIfNewOutboundOrderIsSavedCorrectly() {

            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    currentUser
            )).thenReturn(testWarehouse);

            when(warehouseContractorService.getContractorById(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    currentUser
            )).thenReturn(testContractor);

            when(warehouseItemService.getItemByIdAndWarehouseId(
                    warehouseItem1.getId(),
                    testWarehouse.getId(),
                    currentUser
            )).thenReturn(warehouseItem1);


            when(warehouseItemService.getItemByIdAndWarehouseId(
                    warehouseItem2.getId(),
                    testWarehouse.getId(),
                    currentUser
            )).thenReturn(warehouseItem2);

            warehouseOrderService.createOrder(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    List.of(
                            orderItemRequest1,
                            orderItemRequest2,
                            duplicateOrderItemRequest
                    ),
                    true,
                    null,
                    currentUser
            );

            verify(warehouseOrderRepository, times(1))
                    .save(warehouseOrderCaptor.capture());
            WarehouseOrder capturedOrder = warehouseOrderCaptor.getValue();

            assertEquals(
                    testWarehouse.getId(),
                    capturedOrder.getWarehouse().getId()
            );
            assertEquals(
                    testContractor.getId(),
                    capturedOrder.getWarehouseContractor().getId()
            );
            assertEquals(
                    WarehouseOrderStatus.WAITING,
                    capturedOrder.getWarehouseOrderStatus()
            );
            assertEquals(
                    WarehouseOrderType.OUTBOUND,
                    capturedOrder.getWarehouseOrderType()
            );

            List<WarehouseOrderItem> orderItems =
                    capturedOrder.getWarehouseOrderItems();
            assertEquals(2, orderItems.size());

            WarehouseOrderItem consolidatedOrderItem1 = orderItems.stream()
                    .filter(item ->
                            Objects.equals(
                                    item.getWarehouseItem().getId(),
                                    warehouseItem1.getId())
                    ).findFirst().orElseThrow();

            assertEquals(
                    7L,
                    consolidatedOrderItem1.getOrderedQuantity()
            );

            WarehouseOrderItem consolidatedOrderItem2 = orderItems.stream()
                    .filter(item ->
                            Objects.equals(
                                    item.getWarehouseItem().getId(),
                                    warehouseItem2.getId())
                    ).findFirst().orElseThrow();

            assertEquals(
                    3L,
                    consolidatedOrderItem2.getOrderedQuantity()
            );
        }

        @Test
        void verifyIfNewInboundOrderIsSavedCorrectly() {
            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    currentUser
            )).thenReturn(testWarehouse);

            when(warehouseContractorService.getContractorById(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    currentUser
            )).thenReturn(testContractor);

            warehouseOrderService.createOrder(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    List.of(
                            orderItemRequest1,
                            orderItemRequest2,
                            duplicateOrderItemRequest
                    ),
                    false,
                    null,
                    currentUser
            );

            verify(warehouseOrderRepository, times(1))
                    .save(warehouseOrderCaptor.capture());
            WarehouseOrder capturedOrder = warehouseOrderCaptor.getValue();

            assertEquals(
                    testWarehouse.getId(),
                    capturedOrder.getWarehouse().getId()
            );
            assertEquals(
                    testContractor.getId(),
                    capturedOrder.getWarehouseContractor().getId()
            );
            assertEquals(
                    WarehouseOrderStatus.WAITING,
                    capturedOrder.getWarehouseOrderStatus()
            );
            assertEquals(
                    WarehouseOrderType.INBOUND,
                    capturedOrder.getWarehouseOrderType()
            );

            List<WarehouseOrderItem> orderItems =
                    capturedOrder.getWarehouseOrderItems();
            assertEquals(0, orderItems.size());
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoWriteAccess() {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateWriteAccess(testWarehouse.getId(), currentUser);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    warehouseOrderService.createOrder(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            List.of(
                                    orderItemRequest1
                            ),
                            false,
                            null,
                            currentUser
                    ));

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex.getMessage());

            verify(warehouseOrderRepository, never()).save(any());
        }

        @Test
        void verifyIfExceptionIsThrownWhenOrderItemRequestsIsEmptyOrNullForOutboundOrderButNotForInboundOrder() {
            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    currentUser
            )).thenReturn(testWarehouse);

            when(warehouseContractorService.getContractorById(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    currentUser
            )).thenReturn(testContractor);


            BusinessValidationException exNull =
                    assertThrows(BusinessValidationException.class, () ->
                            warehouseOrderService.createOrder(
                                    testWarehouse.getId(),
                                    testContractor.getId(),
                                    null,
                                    true,
                                    null,
                                    currentUser
                            ));

            assertEquals(
                    "Order must contain at least one item",
                    exNull.getMessage()
            );

            BusinessValidationException exEmpty =
                    assertThrows(BusinessValidationException.class, () ->
                            warehouseOrderService.createOrder(
                                    testWarehouse.getId(),
                                    testContractor.getId(),
                                    Collections.emptyList(),
                                    true,
                                    null,
                                    currentUser
                            ));

            assertEquals(
                    "Order must contain at least one item",
                    exEmpty.getMessage()
            );

            verify(warehouseOrderRepository, never()).save(any());

            assertDoesNotThrow(() ->
                    warehouseOrderService.createOrder(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            Collections.emptyList(),
                            false,
                            null,
                            currentUser
                    ));

            assertDoesNotThrow(() ->
                    warehouseOrderService.createOrder(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            null,
                            false,
                            null,
                            currentUser
                    ));
        }

        @Test
        void verifyIfExceptionIsThrownWhenItemAmountIsLessThanOrEqualToZeroForOutboundOrder() {
            when(warehouseService.getWarehouseByIdAndUser(
                    testWarehouse.getId(),
                    currentUser
            )).thenReturn(testWarehouse);

            when(warehouseContractorService.getContractorById(
                    testWarehouse.getId(),
                    testContractor.getId(),
                    currentUser
            )).thenReturn(testContractor);

            orderItemRequest1.setQuantity(0L);
            orderItemRequest2.setQuantity(-3L);

            BusinessValidationException exZero =
                    assertThrows(BusinessValidationException.class,
                            () -> warehouseOrderService.createOrder(
                                    testWarehouse.getId(),
                                    testContractor.getId(),
                                    List.of(
                                            orderItemRequest1
                                    ),
                                    true,
                                    null,
                                    currentUser
                            ));

            assertEquals(
                    "Quantity must be greater than zero",
                    exZero.getMessage()
            );

            BusinessValidationException exNegative =
                    assertThrows(BusinessValidationException.class,
                            () -> warehouseOrderService.createOrder(
                                    testWarehouse.getId(),
                                    testContractor.getId(),
                                    List.of(
                                            orderItemRequest2
                                    ),
                                    true,
                                    null,
                                    currentUser
                            ));

            assertEquals(
                    "Quantity must be greater than zero",
                    exNegative.getMessage()
            );

            verify(warehouseOrderRepository, never()).save(any());
        }
    }

    @Nested
    class ChangeOrderStatusTests {
        @Test
        void verifyIfStatusIsCorrectlyChanged() {
            when(warehouseOrderRepository.findByIdAndWarehouseId(
                    testOrderInbound.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.of(testOrderInbound));

            warehouseOrderService.changeOrderStatus(
                    testWarehouse.getId(),
                    testOrderInbound.getId(),
                    WarehouseOrderStatus.IN_PROGRESS,
                    currentUser
            );

            verify(
                    warehouseOrderRepository,
                    times(1)
            ).save(warehouseOrderCaptor.capture());

            WarehouseOrder capturedOrder = warehouseOrderCaptor.getValue();
            assertEquals(testOrderInbound.getId(), capturedOrder.getId());
            assertEquals(
                    WarehouseOrderStatus.IN_PROGRESS,
                    capturedOrder.getWarehouseOrderStatus()
            );
        }

        @Test
        void verifyIfNothingIsSavedWhenStatusIsNotChanged() {
            when(warehouseOrderRepository.findByIdAndWarehouseId(
                    testOrderInbound.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.of(testOrderInbound));

            warehouseOrderService.changeOrderStatus(
                    testWarehouse.getId(),
                    testOrderInbound.getId(),
                    testOrderInbound.getWarehouseOrderStatus(),
                    currentUser
            );

            verify(warehouseOrderRepository, never())
                    .save(any(WarehouseOrder.class));
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoWriteAccess() {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateWriteAccess(testWarehouse.getId(), currentUser);

            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseOrderService.changeOrderStatus(
                                    testWarehouse.getId(),
                                    testOrderInbound.getId(),
                                    WarehouseOrderStatus.IN_PROGRESS,
                                    currentUser
                            ));

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex.getMessage()
            );
        }
    }

    @Nested
    class DeleteOrderTests {

        @Test
        void verifyIfOrderIsCorrectlyDeleted() {
            when(warehouseOrderRepository.findByIdAndWarehouseId(
                    testOrderInbound.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.of(testOrderInbound));

            warehouseOrderService.deleteOrder(
                    testWarehouse.getId(),
                    testOrderInbound.getId(),
                    currentUser
            );

            verify(
                    warehouseOrderRepository,
                    times(1)
            ).delete(warehouseOrderCaptor.capture());

            WarehouseOrder capturedOrder = warehouseOrderCaptor.getValue();

            assertEquals(testOrderInbound.getId(), capturedOrder.getId());
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoWriteAccess() {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            )).when(warehouseService)
                    .validateWriteAccess(testWarehouse.getId(), currentUser);

            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseOrderService.deleteOrder(
                                    testWarehouse.getId(),
                                    testOrderInbound.getId(),
                                    currentUser
                            )
                    );

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex.getMessage()
            );
        }
    }
}

package me.mmtr.xwarehouse.service;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.*;
import me.mmtr.xwarehouse.model.dto.OrderItemRequest;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderStatus;
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
    private WarehouseOrder testOrder;
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

        testOrder = new WarehouseOrder();
        testOrder.setId(4L);
        testOrder.setWarehouse(testWarehouse);
        testOrder.setWarehouseContractor(testContractor);
        testOrder.setWarehouseOrderStatus(WarehouseOrderStatus.WAITING);
    }

    @Nested
    class GetOrderByIdTests {
        @BeforeEach
        public void setUp() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    false
            )).thenReturn(true);
        }

        @Test
        void verifyIfOrderIsCorrectlyReturned() {
            when(warehouseOrderRepository.findByIdAndWarehouseId(
                    testOrder.getId(),
                    testWarehouse.getId())
            ).thenReturn(Optional.of(testOrder));

            WarehouseOrder order = warehouseOrderService.getOrderById(
                    testOrder.getId(), testWarehouse.getId(), currentUser
            );

            assertNotNull(order);
            assertEquals(testOrder.getId(), order.getId());
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoReadAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    false
            )).thenReturn(false);

            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseOrderService.getOrderById(
                                    testOrder.getId(),
                                    testWarehouse.getId(),
                                    currentUser
                            ));

            assertEquals("You are not permitted to perform" +
                    " modifications in this warehouse", ex.getMessage());
        }

        @Test
        void verifyIfExceptionIsThrownWhenOrderNotFound() {
            when(warehouseOrderRepository.findByIdAndWarehouseId(
                    testOrder.getId(),
                    testWarehouse.getId())
            ).thenReturn(Optional.empty());

            ResourceNotFoundException ex =
                    assertThrows(ResourceNotFoundException.class, () ->
                            warehouseOrderService.getOrderById(
                                    testOrder.getId(),
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
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    false
            )).thenReturn(true);

            when(warehouseOrderRepository.findAllByWarehouseIdAndWarehouseContractorId(
                    testWarehouse.getId(), testContractor.getId()
            )).thenReturn(List.of(testOrder));

            List<WarehouseOrder> orders =
                    warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                            testWarehouse.getId(), testContractor.getId(), currentUser
                    );

            assertEquals(1, orders.size());
            assertEquals(testOrder.getId(), orders.getFirst().getId());
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoReadAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    false
            )).thenReturn(false);

            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                                    testWarehouse.getId(),
                                    testContractor.getId(),
                                    currentUser
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

            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);
        }

        @Test
        void verifyIfNewOrderIsSavedCorrectly() {

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
                    currentUser
            );

            verify(warehouseOrderRepository).save(warehouseOrderCaptor.capture());
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
        void verifyIfExceptionIsThrownWhenNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(false);

            ResourceAccessException ex = assertThrows(ResourceAccessException.class, () ->
                    warehouseOrderService.createOrder(
                            testWarehouse.getId(),
                            testContractor.getId(),
                            List.of(
                                    orderItemRequest1
                            ),
                            currentUser
                    ));

            assertEquals(
                    "You are not permitted to perform" +
                            " modifications in this warehouse",
                    ex.getMessage());

            verify(warehouseOrderRepository, never()).save(any());
        }

        @Test
        void verifyIfExceptionIsThrownWhenOrderItemRequestsIsEmptyOrNull() {
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
                                    currentUser
                            ));

            assertEquals(
                    "Order must contain at least one item",
                    exEmpty.getMessage()
            );

            verify(warehouseOrderRepository, never()).save(any());
        }

        @Test
        void verifyIfExceptionIsThrownWhenItemAmountIsLessThanOrEqualToZero() {
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
        @BeforeEach
        public void setUp() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);
        }


        @Test
        void verifyIfStatusIsCorrectlyChanged() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    false
            )).thenReturn(true);

            when(warehouseOrderRepository.findByIdAndWarehouseId(
                    testOrder.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.of(testOrder));

            warehouseOrderService.changeOrderStatus(
                    testWarehouse.getId(),
                    testOrder.getId(),
                    WarehouseOrderStatus.IN_PROGRESS,
                    currentUser
            );

            verify(
                    warehouseOrderRepository,
                    times(1)
            ).save(warehouseOrderCaptor.capture());

            WarehouseOrder capturedOrder = warehouseOrderCaptor.getValue();
            assertEquals(testOrder.getId(), capturedOrder.getId());
            assertEquals(
                    WarehouseOrderStatus.IN_PROGRESS,
                    capturedOrder.getWarehouseOrderStatus()
            );
        }

        @Test
        void verifyIfNothingIsSavedWhenStatusIsNotChanged() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    false
            )).thenReturn(true);

            when(warehouseOrderRepository.findByIdAndWarehouseId(
                    testOrder.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.of(testOrder));

            warehouseOrderService.changeOrderStatus(
                    testWarehouse.getId(),
                    testOrder.getId(),
                    testOrder.getWarehouseOrderStatus(),
                    currentUser
            );

            verify(warehouseOrderRepository, never())
                    .save(any(WarehouseOrder.class));
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(false);

            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseOrderService.changeOrderStatus(
                                    testWarehouse.getId(),
                                    testOrder.getId(),
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
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    false
            )).thenReturn(true);

            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(true);

            when(warehouseOrderRepository.findByIdAndWarehouseId(
                    testOrder.getId(),
                    testWarehouse.getId()
            )).thenReturn(Optional.of(testOrder));

            warehouseOrderService.deleteOrder(
                    testWarehouse.getId(),
                    testOrder.getId(),
                    currentUser
            );

            verify(
                    warehouseOrderRepository,
                    times(1)
            ).delete(warehouseOrderCaptor.capture());

            WarehouseOrder capturedOrder = warehouseOrderCaptor.getValue();

            assertEquals(testOrder.getId(), capturedOrder.getId());
        }

        @Test
        void verifyIfExceptionIsThrownWhenNoWriteAccess() {
            when(warehouseService.hasAccess(
                    testWarehouse.getId(),
                    currentUser.getId(),
                    true
            )).thenReturn(false);

            ResourceAccessException ex =
                    assertThrows(ResourceAccessException.class, () ->
                            warehouseOrderService.deleteOrder(
                                    testWarehouse.getId(),
                                    testOrder.getId(),
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

package me.mmtr.xwarehouse.service;

import jakarta.transaction.Transactional;
import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.*;
import me.mmtr.xwarehouse.model.dto.OrderItemRequest;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderStatus;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderType;
import me.mmtr.xwarehouse.repository.WarehouseOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WarehouseOrderService {
    private final WarehouseOrderRepository warehouseOrderRepository;
    private final WarehouseService warehouseService;
    private final WarehouseContractorService warehouseContractorService;
    private final WarehouseItemService warehouseItemService;

    public WarehouseOrderService(
            WarehouseOrderRepository warehouseOrderRepository,
            WarehouseService warehouseService,
            WarehouseContractorService warehouseContractorService, WarehouseItemService warehouseItemService) {
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.warehouseService = warehouseService;
        this.warehouseContractorService = warehouseContractorService;
        this.warehouseItemService = warehouseItemService;
    }

    public WarehouseOrder getOrderById(
            Long orderId,
            Long warehouseId,
            User currentUser
    ) {
        warehouseService.validateReadAccess(warehouseId, currentUser);

        Optional<WarehouseOrder> warehouseOrder =
                warehouseOrderRepository.findByIdAndWarehouseId(
                        orderId,
                        warehouseId
                );

        if (warehouseOrder.isPresent()) {
            return warehouseOrder.get();
        }

        throw new ResourceNotFoundException("Order with provided id not found");
    }

    public List<WarehouseOrder> getOrdersByWarehouseIdAndContractorId(
            Long warehouseId,
            Long contractorId,
            User currentUser,
            boolean outbound
    ) {
        warehouseService.validateReadAccess(warehouseId, currentUser);
        return outbound ?
                warehouseOrderRepository.findAllByWarehouseIdAndWarehouseContractorIdAndWarehouseOrderType(warehouseId, contractorId, WarehouseOrderType.OUTBOUND)
                :
                warehouseOrderRepository.findAllByWarehouseIdAndWarehouseContractorIdAndWarehouseOrderType(warehouseId, contractorId, WarehouseOrderType.INBOUND);
    }

    @Transactional
    public void createOrder(
            Long warehouseId,
            Long warehouseContractorId,
            List<OrderItemRequest> orderItemRequests,
            boolean outbound,
            String additionalInfo,
            User currentUser
    ) {
        warehouseService.validateWriteAccess(warehouseId, currentUser);

        Warehouse warehouse =
                warehouseService.getWarehouseByIdAndUser(warehouseId, currentUser);

        WarehouseContractor contractor = warehouseContractorService.getContractorById(
                warehouseId, warehouseContractorId, currentUser
        );

        WarehouseOrder warehouseOrder = new WarehouseOrder();
        warehouseOrder.setWarehouse(warehouse);
        warehouseOrder.setWarehouseContractor(contractor);
        warehouseOrder.setWarehouseOrderStatus(WarehouseOrderStatus.WAITING);

        if (outbound) {
            warehouseOrder.setWarehouseOrderType(WarehouseOrderType.OUTBOUND);

            if (orderItemRequests == null || orderItemRequests.isEmpty()) {
                throw new BusinessValidationException(
                        "Order must contain at least one item"
                );
            }

            Map<Long, Long> consolidatedItems = orderItemRequests.stream()
                    .filter(req ->
                            req.getWarehouseItemId() != null && req.getQuantity() != null
                    )
                    .collect(Collectors.groupingBy(
                            OrderItemRequest::getWarehouseItemId,
                            Collectors.summingLong(OrderItemRequest::getQuantity)
                    ));

            consolidatedItems.forEach((itemId, quantity) -> {
                if (quantity <= 0) {
                    throw new BusinessValidationException(
                            "Quantity must be greater than zero"
                    );
                }

                WarehouseItem warehouseItem =
                        warehouseItemService.getItemByIdAndWarehouseId(
                                itemId,
                                warehouseId,
                                currentUser
                        );

                WarehouseOrderItem warehouseOrderItem = new WarehouseOrderItem();
                warehouseOrderItem.setWarehouseOrder(warehouseOrder);
                warehouseOrderItem.setWarehouseItem(warehouseItem);
                warehouseOrderItem.setOrderedQuantity(quantity);

                warehouseOrder.getWarehouseOrderItems().add(warehouseOrderItem);
            });
        } else {
            warehouseOrder.setWarehouseOrderType(WarehouseOrderType.INBOUND);
        }

        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            warehouseOrder.setAdditionalInfo(additionalInfo);
        }

        warehouseOrderRepository.save(warehouseOrder);
    }

    @Transactional
    public void changeOrderStatus(
            Long warehouseId,
            Long orderId,
            WarehouseOrderStatus newStatus,
            User currentUser
    ) {
        warehouseService.validateWriteAccess(warehouseId, currentUser);

        WarehouseOrder order =
                getOrderById(orderId, warehouseId, currentUser);

        if (newStatus.equals(order.getWarehouseOrderStatus())) return;

        order.setWarehouseOrderStatus(newStatus);
        warehouseOrderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long warehouseId, Long orderId, User currentUser) {
        warehouseService.validateWriteAccess(warehouseId, currentUser);

        WarehouseOrder order = getOrderById(orderId, warehouseId, currentUser);

        warehouseOrderRepository.delete(order);
    }
}

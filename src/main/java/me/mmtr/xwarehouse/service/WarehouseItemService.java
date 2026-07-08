package me.mmtr.xwarehouse.service;

import jakarta.transaction.Transactional;
import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseItem;
import me.mmtr.xwarehouse.model.WarehouseZone;
import me.mmtr.xwarehouse.repository.WarehouseItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WarehouseItemService {

    private final WarehouseZoneService zoneService;
    private final WarehouseItemRepository itemRepository;
    private final WarehouseService warehouseService;

    public WarehouseItemService(
            WarehouseZoneService zoneService,
            WarehouseItemRepository itemRepository,
            WarehouseService warehouseService
    ) {
        this.zoneService = zoneService;
        this.itemRepository = itemRepository;
        this.warehouseService = warehouseService;
    }

    public WarehouseItem getItemByIdAndWarehouseId(
            Long itemId,
            Long warehouseId,
            User currentUser
    ) {
        validateReadAccess(warehouseId, currentUser);

        Optional<WarehouseItem> itemOptional =
                itemRepository.findByIdAndWarehouseZoneWarehouseId(
                        itemId,
                        warehouseId
                );

        if (itemOptional.isPresent()) {
            return itemOptional.get();
        }

        throw new ResourceNotFoundException("Item with provided id not found");
    }

    public List<WarehouseItem> getItemsByWarehouseId(
            Long warehouseId,
            User currentUser
    ) {
        validateReadAccess(warehouseId, currentUser);
        return itemRepository.findAllByWarehouseZoneWarehouseId(warehouseId);
    }

    public List<WarehouseItem> getItemsByWarehouseIdAndUser(
            Long warehouseId,
            Long zoneId,
            String keyword,
            User currentUser
    ) {
        // checks whether the provided user has the required permissions
        //warehouseService.getWarehouseByIdAndUser(warehouseId, currentUser);
        validateReadAccess(warehouseId, currentUser);

        if (keyword == null || keyword.trim().isEmpty()) {
            return itemRepository.findAllByWarehouseZoneId(zoneId);
        }

        return itemRepository.findAllByZoneIdAndNameOrSkuContaining(
                zoneId,
                keyword
        );
    }

    @Transactional
    public void addItemToZone(
            Long warehouseId,
            Long zoneId,
            String name,
            String sku,
            Long quantity,
            User currentUser) {
        validateWriteAccess(warehouseId, currentUser);

        WarehouseZone zone =
                zoneService.getZoneByIdAndUser(warehouseId, zoneId, currentUser);

        Optional<WarehouseItem> warehouseItemOptional =
                itemRepository.findByWarehouseZoneWarehouseIdAndSku(warehouseId, sku);

        if (warehouseItemOptional.isPresent()) {
            WarehouseItem foundItem = warehouseItemOptional.get();
            foundItem.setQuantity(foundItem.getQuantity() + quantity);

            itemRepository.save(foundItem);
        } else {
            WarehouseItem warehouseItem = new WarehouseItem(
                    name, sku, quantity, zone
            );

            itemRepository.save(warehouseItem);
        }
    }

    @Transactional
    public void updateItemInZone(
            Long warehouseId,
            Long itemId,
            String newName,
            String newSku,
            Long newQuantity,
            Long zoneId,
            User currentUser
    ) {
        validateWriteAccess(warehouseId, currentUser);

        WarehouseItem item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Item does not exist")
                );

        if (!item.getWarehouseZone().getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException(
                    "Item does not belong to this warehouse"
            );
        }

        WarehouseZone zone = zoneService
                .getZoneByIdAndUser(
                        warehouseId,
                        zoneId,
                        currentUser
                );

        if (!item.getSku().equals(newSku)) {
            itemRepository.findByWarehouseZoneWarehouseIdAndSku(warehouseId, newSku)
                    .ifPresent(warehouseItem -> {
                        throw new BusinessValidationException("Item with" +
                                " SKU " + newSku + " already exists"
                        );
                    });
        }

        item.setName(newName);
        item.setSku(newSku);
        item.setQuantity(newQuantity);
        item.setWarehouseZone(zone);

        itemRepository.save(item);
    }

    @Transactional
    public void removeItemFromZone(Long warehouseId, Long itemId, User currentUser) {
        Warehouse warehouse =
                warehouseService.getWarehouseByIdAndUser(warehouseId, currentUser);

        validateWriteAccess(warehouseId, currentUser);

        WarehouseItem warehouseItem = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Item does not exist"));

        if (!warehouseItem.getWarehouseZone().getWarehouse().getId().equals(warehouse.getId())) {
            throw new ResourceNotFoundException(
                    "Item does not exist in this warehouse"
            );
        }

        itemRepository.delete(warehouseItem);
    }

    private void validateReadAccess(Long warehouseId, User currentUser) {
        if (!warehouseService.hasAccess(warehouseId, currentUser.getId(), false)) {
            throw new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            );
        }
    }

    private void validateWriteAccess(Long warehouseId, User currentUser) {
        if (!warehouseService.hasAccess(
                warehouseId,
                currentUser.getId(),
                true
        )) {
            throw new ResourceAccessException(
                    "You are not permitted to perform" +
                            " modifications in this warehouse"
            );
        }
    }
}

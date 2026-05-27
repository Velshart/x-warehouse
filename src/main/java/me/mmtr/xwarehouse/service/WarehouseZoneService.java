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
import me.mmtr.xwarehouse.repository.WarehouseRepository;
import me.mmtr.xwarehouse.repository.WarehouseZoneRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WarehouseZoneService {

    private final WarehouseZoneRepository zoneRepository;
    private final WarehouseItemRepository itemRepository;
    private final WarehouseService warehouseService;
    private final WarehouseRepository warehouseRepository;

    public WarehouseZoneService(
            WarehouseZoneRepository zoneRepository,
            WarehouseItemRepository itemRepository,
            WarehouseService warehouseService,
            WarehouseRepository warehouseRepository
    ) {
        this.zoneRepository = zoneRepository;
        this.itemRepository = itemRepository;
        this.warehouseService = warehouseService;
        this.warehouseRepository = warehouseRepository;
    }

    public WarehouseZone getZoneByIdAndUser(Long warehouseId, Long zoneId, User currentUser) {
        Warehouse warehouse
                = warehouseService.getWarehouseByIdAndUser(warehouseId, currentUser);

        WarehouseZone warehouseZone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Zone with id " + zoneId + " does not exist"
                ));

        if (!warehouseZone.getWarehouse().getId().equals(warehouse.getId())) {
            throw new ResourceNotFoundException(
                    "Requested zone does not belong to the specified warehouse"
            );
        }

        return warehouseZone;
    }

    @Transactional
    public void addZone(Long warehouseId, String name, User currentUser) {
        Warehouse warehouse =
                warehouseService.getWarehouseByIdAndUser(warehouseId, currentUser);

        validateWriteAccess(warehouseId, currentUser);

        zoneRepository.findByWarehouseIdAndName(warehouseId, name)
                .ifPresent(zone -> {
                    throw new BusinessValidationException(
                            "Zone with name '" + name + "' already exists in this warehouse"
                    );
                });

        WarehouseZone warehouseZone = new WarehouseZone(name, warehouse);

        zoneRepository.save(warehouseZone);
    }

    @Transactional
    public void deleteZone(Long warehouseId, Long zoneId, User currentUser) {
        validateWriteAccess(warehouseId, currentUser);

        WarehouseZone warehouseZone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Zone not found"
                ));

        if (!warehouseZone.getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException(
                    "This Zone does not exist in this warehouse"
            );
        }

        zoneRepository.delete(warehouseZone);
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
                zoneRepository.findById(zoneId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Zone with name " + name +
                                                " does not exist in this warehouse"
                                )
                        );

        Optional<WarehouseItem> warehouseItemOptional = itemRepository.findBySku(sku);

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
    public void removeItemFromZone(Long warehouseId, Long itemId, User currentUser) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warehouse does not exist"
                ));

        validateWriteAccess(warehouseId, currentUser);

        WarehouseItem warehouseItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item does not exist"));

        if (!warehouseItem.getWarehouseZone().getWarehouse().getId().equals(warehouse.getId())) {
            throw new ResourceNotFoundException("Item does not exist in this warehouse");
        }

        itemRepository.delete(warehouseItem);
    }

    private void validateWriteAccess(Long warehouseId, User currentUser) {
        if (!warehouseService.hasAccess(
                warehouseId,
                currentUser.getId(),
                true
        )) {
            throw new ResourceAccessException(
                    "You are not permitted to perform modifications in this warehouse"
            );
        }
    }
}

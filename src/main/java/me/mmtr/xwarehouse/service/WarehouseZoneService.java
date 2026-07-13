package me.mmtr.xwarehouse.service;

import jakarta.transaction.Transactional;
import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseZone;
import me.mmtr.xwarehouse.repository.WarehouseZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseZoneService {

    private final WarehouseZoneRepository zoneRepository;
    private final WarehouseService warehouseService;

    public WarehouseZoneService(
            WarehouseZoneRepository zoneRepository,
            WarehouseService warehouseService
    ) {
        this.zoneRepository = zoneRepository;
        this.warehouseService = warehouseService;
    }

    public WarehouseZone getZoneByIdAndUser(
            Long warehouseId,
            Long zoneId,
            User currentUser
    ) {
        //validateReadAccess(warehouseId, currentUser);
        warehouseService.validateReadAccess(warehouseId, currentUser);

        Warehouse warehouse
                = warehouseService.getWarehouseByIdAndUser(warehouseId, currentUser);

        WarehouseZone warehouseZone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Zone not found"
                ));

        if (!warehouseZone.getWarehouse().getId().equals(warehouse.getId())) {
            throw new ResourceNotFoundException(
                    "Requested zone does not belong to the specified warehouse"
            );
        }

        return warehouseZone;
    }

    public List<WarehouseZone> getZonesByWarehouseId(
            Long warehouseId,
            String keyword,
            User currentUser
    ) {
        //validateReadAccess(warehouseId, currentUser);
        warehouseService.validateReadAccess(warehouseId, currentUser);

        if (keyword == null || keyword.trim().isEmpty()) {
            return zoneRepository.findAllByWarehouseId(warehouseId);
        }

        return zoneRepository.findAllByWarehouseIdAndNameContaining(
                warehouseId,
                keyword
        );
    }

    @Transactional
    public void addZone(Long warehouseId, String name, User currentUser) {
        Warehouse warehouse =
                warehouseService.getWarehouseByIdAndUser(warehouseId, currentUser);
        //TODO: check ^

        warehouseService.validateWriteAccess(warehouseId, currentUser);

        zoneRepository.findByWarehouseIdAndName(warehouseId, name)
                .ifPresent(zone -> {
                    throw new BusinessValidationException(
                            "Zone with name " + name + " already exists" +
                                    " in this warehouse"
                    );
                });

        WarehouseZone warehouseZone = new WarehouseZone(name, warehouse);

        zoneRepository.save(warehouseZone);
    }

    @Transactional
    public void updateZone(
            Long warehouseId,
            Long zoneId,
            String newName,
            User currentUser
    ) {
        warehouseService.validateWriteAccess(warehouseId, currentUser);

        WarehouseZone warehouseZone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));

        if (!warehouseZone.getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException(
                    "Requested zone does not belong to the specified warehouse"
            );
        }

        warehouseZone.setName(newName);
        zoneRepository.save(warehouseZone);
    }

    @Transactional
    public void deleteZone(Long warehouseId, Long zoneId, User currentUser) {
        warehouseService.validateWriteAccess(warehouseId, currentUser);

        WarehouseZone warehouseZone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Zone not found"
                ));

        if (!warehouseZone.getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException(
                    "Requested zone does not belong to the specified warehouse"
            );
        }

        zoneRepository.delete(warehouseZone);
    }
}

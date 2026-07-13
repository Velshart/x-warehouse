package me.mmtr.xwarehouse.service;

import jakarta.transaction.Transactional;
import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseContractor;
import me.mmtr.xwarehouse.repository.WarehouseContractorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WarehouseContractorService {

    private final WarehouseContractorRepository contractorRepository;
    private final WarehouseService warehouseService;

    public WarehouseContractorService(
            WarehouseContractorRepository contractorRepository,
            WarehouseService warehouseService
    ) {
        this.contractorRepository = contractorRepository;
        this.warehouseService = warehouseService;
    }

    public List<WarehouseContractor> getContractorsByWarehouseId(
            Long warehouseId,
            User currentUser
    ) {
        warehouseService.validateReadAccess(warehouseId, currentUser);
        return contractorRepository.findAllByWarehouseId(warehouseId);
    }

    public WarehouseContractor getContractorById(
            Long warehouseId,
            Long contractorId,
            User currentUser
    ) {
        warehouseService.validateReadAccess(warehouseId, currentUser);

        Optional<WarehouseContractor> contractorOptional =
                contractorRepository.findByIdAndWarehouseId(
                        contractorId,
                        warehouseId
                );

        if (contractorOptional.isPresent()) {
            return contractorOptional.get();
        }
        throw new ResourceNotFoundException("Contractor with provided id not found");
    }

    @Transactional
    public void addContractorToWarehouse(
            Long warehouseId,
            String code,
            String name,
            String nip,
            String additionalInfo,
            User currentUser
    ) {
        warehouseService.validateWriteAccess(warehouseId, currentUser);

        Warehouse warehouse =
                warehouseService.getWarehouseByIdAndUser(warehouseId, currentUser);

        contractorRepository.findByWarehouseIdAndNip(warehouseId, nip)
                .ifPresent(c -> {
                    throw new BusinessValidationException(
                            "Contractor with provided NIP " +
                                    " already exists in this warehouse"
                    );
                });

        WarehouseContractor contractor = new WarehouseContractor(
                code, name, nip, additionalInfo, warehouse
        );
        contractorRepository.save(contractor);
    }

    @Transactional
    public void removeContractorFromWarehouse(
            Long warehouseId,
            Long contractorId,
            User currentUser
    ) {
        warehouseService.validateWriteAccess(warehouseId, currentUser);
        WarehouseContractor contractor =
                contractorRepository.findByIdAndWarehouseId(
                        contractorId, warehouseId
                ).orElseThrow(() ->
                        new ResourceNotFoundException("Contractor not found")
                );

        contractorRepository.delete(contractor);
    }

    @Transactional
    public void updateContractor(
            Long warehouseId,
            Long contractorId,
            String code,
            String name,
            String nip,
            String additionalInfo,
            User currentUser
    ) {
        warehouseService.validateWriteAccess(warehouseId, currentUser);

        WarehouseContractor contractor =
                contractorRepository
                        .findByIdAndWarehouseId(contractorId, warehouseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Contractor not found")
                        );

        Warehouse warehouse = warehouseService.getWarehouseByIdAndUser(
                warehouseId,
                currentUser
        );

        if (!contractor.getNip().equals(nip)) {
            contractorRepository.findByWarehouseIdAndNip(warehouseId, nip).ifPresent(
                    c -> {
                        throw new BusinessValidationException(
                                "Contractor with provided NIP already exists" +
                                        " in this warehouse"
                        );
                    }
            );
        }

        contractor.setCode(code);
        contractor.setName(name);
        contractor.setNip(nip);
        contractor.setAdditionalInfo(additionalInfo);
        contractor.setWarehouse(warehouse);

        contractorRepository.save(contractor);
    }
}

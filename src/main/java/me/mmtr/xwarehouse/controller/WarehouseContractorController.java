package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.service.WarehouseContractorService;
import me.mmtr.xwarehouse.service.WarehouseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/warehouse/{warehouseId}/contractors")
public class WarehouseContractorController {

    private final WarehouseService warehouseService;
    private final WarehouseContractorService contractorService;

    public WarehouseContractorController(WarehouseService warehouseService, WarehouseContractorService warehouseContractorService) {
        this.warehouseService = warehouseService;
        this.contractorService = warehouseContractorService;
    }

    @GetMapping
    public String contractors(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            Model model
    ) {
        model.addAttribute(
                "warehouse",
                warehouseService.getWarehouseByIdAndUser(warehouseId, currentUser)
        );
        model.addAttribute(
                "contractors",
                contractorService.getContractorsByWarehouseId(
                        warehouseId,
                        currentUser
                )
        );
        boolean canManage = warehouseService.hasAccess(
                warehouseId,
                currentUser.getId(),
                true
        );
        model.addAttribute("canManage", canManage);
        model.addAttribute("currentUser", currentUser);
        return "manage-contractors";
    }

    @PostMapping("/add-contractor")
    public String addContractor(
            @PathVariable Long warehouseId,
            @AuthenticationPrincipal User currentUser,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam String nip,
            @RequestParam(required = false) String additionalInfo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            contractorService.addContractorToWarehouse(
                    warehouseId,
                    code,
                    name,
                    nip,
                    additionalInfo,
                    currentUser
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Contractor added successfully"
            );
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }
        return "redirect:/warehouse/" + warehouseId + "/contractors";
    }

    @PostMapping("/update-contractor")
    public String updateContractor(
            @PathVariable Long warehouseId,
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long contractorId,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam String nip,
            @RequestParam(required = false) String additionalInfo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            contractorService.updateContractor(
                    warehouseId, contractorId, code, name, nip, additionalInfo, currentUser

            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Contractor updated successfully"
            );
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }
        return "redirect:/warehouse/" + warehouseId + "/contractors";
    }

    @PostMapping("/delete-contractor")
    public String deleteContractor(
            @PathVariable Long warehouseId,
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long contractorId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            contractorService.removeContractorFromWarehouse(warehouseId, contractorId, currentUser);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Contractor deleted successfully"
            );
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }
        return "redirect:/warehouse/" + warehouseId + "/contractors";
    }
}

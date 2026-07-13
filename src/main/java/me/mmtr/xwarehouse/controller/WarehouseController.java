package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseZone;
import me.mmtr.xwarehouse.model.enums.WarehouseRole;
import me.mmtr.xwarehouse.service.WarehouseService;
import me.mmtr.xwarehouse.service.WarehouseZoneService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseZoneService zoneService;

    public WarehouseController(
            WarehouseService warehouseService,
            WarehouseZoneService zoneService
    ) {
        this.warehouseService = warehouseService;
        this.zoneService = zoneService;
    }

    @GetMapping("/{id}")
    public String warehouse(
            @AuthenticationPrincipal User currentUser,
            @PathVariable(name = "id") Long id,
            @RequestParam(required = false) String search,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            Warehouse warehouse =
                    warehouseService.getWarehouseByIdAndUser(id, currentUser);

            List<WarehouseZone> zones = zoneService.getZonesByWarehouseId(
                    warehouse.getId(),
                    search,
                    currentUser
            );

            boolean isOwner = warehouse.getOwner().getId().equals(currentUser.getId());

            boolean isAdmin = !isOwner && warehouseService.hasAccess(id, currentUser.getId(), true);

            boolean isViewer = !isOwner && !isAdmin;

            model.addAttribute("currentUser", currentUser);
            model.addAttribute("zones", zones);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isViewer", isViewer);
            model.addAttribute("canManage", isOwner || isAdmin);
            model.addAttribute("search", search);

            model.addAttribute("warehouse", warehouse);

            return "manage-warehouse";
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/dashboard";
        }
    }

    @PostMapping("/create")
    public String createWarehouse(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("name") String name,
            RedirectAttributes redirectAttributes
    ) {
        try {
            warehouseService.createWarehouse(name, currentUser);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Warehouse created successfully"
            );
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/update")
    public String updateWarehouse(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long warehouseId,
            @RequestParam String name,
            RedirectAttributes redirectAttributes
    ) {
        try {
            warehouseService.updateWarehouse(warehouseId, name, currentUser);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Warehouse updated successfully"
            );
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/delete")
    public String deleteWarehouse(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("warehouseId") Long warehouseId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            warehouseService.deleteWarehouse(warehouseId, currentUser);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Warehouse deleted successfully"
            );
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/add-member")
    public String addMember(
            @AuthenticationPrincipal User currentUser,
            @PathVariable(name = "id") Long warehouseId,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "role") String role,
            RedirectAttributes redirectAttributes
    ) {
        try {
            WarehouseRole memberRole = WarehouseRole.valueOf(role.toUpperCase());

            warehouseService.addMember(warehouseId, username, memberRole, currentUser);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "New member added successfully"
            );
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }
        return "redirect:/warehouse/" + warehouseId;
    }

    @PostMapping("/{id}/remove-member")
    public String removeMember(
            @AuthenticationPrincipal User currentUser,
            @PathVariable(name = "id") Long warehouseId,
            @RequestParam(name = "userId") Long userId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            warehouseService.removeMember(warehouseId, userId, currentUser);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Member removed successfully"
            );
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }
        return "redirect:/warehouse/" + warehouseId;
    }
}

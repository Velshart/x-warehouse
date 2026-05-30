package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseZone;
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
public class WarehouseZoneController {
    private final WarehouseZoneService zoneService;
    private final WarehouseService warehouseService;

    public WarehouseZoneController(
            WarehouseZoneService warehouseZoneService,
            WarehouseService warehouseService
    ) {
        this.zoneService = warehouseZoneService;
        this.warehouseService = warehouseService;
    }

    @GetMapping("/{warehouseId}/zone/{zoneId}")
    public String viewZone(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @PathVariable Long zoneId,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            List<WarehouseZone> zonesList =
                    zoneService.getZonesByWarehouseId(warehouseId, currentUser);

            WarehouseZone zone = zoneService.getZoneByIdAndUser(
                    warehouseId, zoneId, currentUser
            );

            Warehouse warehouse = zone.getWarehouse();

            boolean isOwner = warehouse.getOwner().getId().equals(currentUser.getId());
            boolean isAdmin = !isOwner &&
                    warehouseService.hasAccess(
                            warehouseId,
                            currentUser.getId(),
                            true
                    );

            model.addAttribute("allZones", zonesList);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("warehouseId", warehouseId);
            model.addAttribute("warehouseName", warehouse.getName());
            model.addAttribute("zone", zone);
            model.addAttribute("canManage", isOwner || isAdmin);

            return "manage-zone";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/warehouse/" + warehouseId;
        }
    }

    @PostMapping("/{warehouseId}/add-zone")
    public String addZone(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @RequestParam String name,
            RedirectAttributes redirectAttributes
    ) {
        try {
            zoneService.addZone(warehouseId, name, currentUser);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "New zone added successfully."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/warehouse/" + warehouseId;
    }

    @PostMapping("/{warehouseId}/update-zone")
    public String updateZone(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @RequestParam Long zoneId,
            @RequestParam String name,
            RedirectAttributes redirectAttributes
    ) {
        try {
            zoneService.updateZone(warehouseId, zoneId, name, currentUser);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Zone updated successfully"
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }
        return "redirect:/warehouse/" + warehouseId;
    }

    @PostMapping("/{warehouseId}/delete-zone")
    public String deleteZone(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @RequestParam Long zoneId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            zoneService.deleteZone(warehouseId, zoneId, currentUser);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Zone deleted successfully."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }
        return "redirect:/warehouse/" + warehouseId;
    }
}

package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.service.WarehouseItemService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/warehouse")
public class WarehouseItemController {
    private final WarehouseItemService itemService;

    public WarehouseItemController(
            WarehouseItemService itemService
    ) {
        this.itemService = itemService;
    }

    @PostMapping("/{warehouseId}/zone/{zoneId}/add-item")
    public String addItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @PathVariable Long zoneId,
            @RequestParam String name,
            @RequestParam String sku,
            @RequestParam Long quantity,
            RedirectAttributes redirectAttributes
    ) {
        try {
            itemService.addItemToZone(
                    warehouseId, zoneId, name, sku, quantity, currentUser);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Item registered successfully."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/warehouse/" + warehouseId + "/zone/" + zoneId;
    }

    @PostMapping("/{warehouseId}/zone/{zoneId}/update-item")
    public String updateItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @PathVariable Long zoneId,
            @RequestParam Long itemId,
            @RequestParam String name,
            @RequestParam String sku,
            @RequestParam Long quantity,
            RedirectAttributes redirectAttributes
    ) {
        try {
            itemService.updateItemInZone(
                    warehouseId, itemId, name, sku, quantity, currentUser
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Item updated successfully."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage()
            );
        }
        return "redirect:/warehouse/" + warehouseId + "/zone/" + zoneId;
    }

    @PostMapping("/{warehouseId}/zone/{zoneId}/remove-item")
    public String removeItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @PathVariable Long zoneId,
            @RequestParam Long itemId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            itemService.removeItemFromZone(warehouseId, itemId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Item removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/warehouse/" + warehouseId + "/zone/" + zoneId;
    }
}

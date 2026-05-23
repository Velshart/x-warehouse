package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.enums.WarehouseRole;
import me.mmtr.xwarehouse.service.WarehouseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/{id}")
    public String warehouse(
            @AuthenticationPrincipal User currentUser,
            @PathVariable(name = "id") Long id,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            Warehouse warehouse =
                    warehouseService.getWarehouseByIdAndUser(id, currentUser);

            boolean isOwner = warehouse.getOwner().getId().equals(currentUser.getId());

            boolean isAdmin = !isOwner && warehouseService.hasAccess(id, currentUser.getId(), true);

            boolean isViewer = !isOwner && !isAdmin;

            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isViewer", isViewer);
            model.addAttribute("canManage", isOwner || isAdmin);

            model.addAttribute("warehouse", warehouse);

            return "manage-warehouse";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/dashboard";
        }
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
            WarehouseRole memberRole = role.equals("ADMIN")
                    ? WarehouseRole.ADMIN : WarehouseRole.VIEWER;

            warehouseService.addMember(warehouseId, username, memberRole, currentUser);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "New member added successfully"
            );
        } catch (Exception e) {
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
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }
        return "redirect:/warehouse/" + warehouseId;
    }
}

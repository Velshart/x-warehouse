package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.service.WarehouseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final WarehouseService warehouseService;

    public DashboardController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public String dashboard(
            @AuthenticationPrincipal User currentUser,
            Model model
    ) {
        List<Warehouse> warehouses = warehouseService
                .getAvailableWarehouses(currentUser.getId());

        model.addAttribute("warehouses", warehouses);
        model.addAttribute("currentUser", currentUser);

        return "dashboard";
    }

    @PostMapping("/warehouse/create")
    public String createWarehouse(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("name") String name,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String warehouseName = name.trim();

            if (warehouseName.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }

            warehouseService.createWarehouse(warehouseName, currentUser);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Warehouse created successfully"
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/warehouse/delete")
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
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

}

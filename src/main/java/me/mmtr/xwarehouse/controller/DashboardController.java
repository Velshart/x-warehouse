package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.service.WarehouseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}

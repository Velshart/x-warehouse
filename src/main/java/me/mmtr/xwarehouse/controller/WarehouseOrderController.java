package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.BusinessValidationException;
import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.WarehouseContractor;
import me.mmtr.xwarehouse.model.WarehouseItem;
import me.mmtr.xwarehouse.model.WarehouseOrder;
import me.mmtr.xwarehouse.model.dto.OrderedItemsForm;
import me.mmtr.xwarehouse.model.enums.WarehouseOrderStatus;
import me.mmtr.xwarehouse.service.WarehouseContractorService;
import me.mmtr.xwarehouse.service.WarehouseItemService;
import me.mmtr.xwarehouse.service.WarehouseOrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/warehouse")
public class WarehouseOrderController {
    private final WarehouseOrderService warehouseOrderService;
    private final WarehouseItemService warehouseItemService;
    private final WarehouseContractorService warehouseContractorService;

    public WarehouseOrderController(WarehouseOrderService warehouseOrderService,
                                    WarehouseItemService warehouseItemService,
                                    WarehouseContractorService warehouseContractorService) {
        this.warehouseOrderService = warehouseOrderService;
        this.warehouseItemService = warehouseItemService;
        this.warehouseContractorService = warehouseContractorService;
    }

    @GetMapping("/{warehouseId}/contractor-outbound-orders/{contractorId}")
    public String getContractorOutboundOrders(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @PathVariable Long contractorId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            List<WarehouseOrder> orders = warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                    warehouseId,
                    contractorId,
                    currentUser,
                    true
            );

            WarehouseContractor contractor =
                    warehouseContractorService.getContractorById(warehouseId, contractorId, currentUser);

            List<WarehouseItem> items =
                    warehouseItemService.getItemsByWarehouseId(warehouseId, currentUser);

            //model.addAttribute("outbound", true);
            model.addAttribute("orderList", orders);
            model.addAttribute("itemList", items);
            model.addAttribute("contractor", contractor);
            model.addAttribute("warehouseId", warehouseId);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute(
                    "statuses",
                    WarehouseOrderStatus.values()
            );

            return "manage-outbound-orders";
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/warehouse/" + warehouseId + "/contractors";
        }
    }

    @GetMapping("/{warehouseId}/contractor-inbound-orders/{contractorId}")
    public String getContractorInboundOrders(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @PathVariable Long contractorId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            List<WarehouseOrder> orders = warehouseOrderService.getOrdersByWarehouseIdAndContractorId(
                    warehouseId,
                    contractorId,
                    currentUser,
                    false
            );

            WarehouseContractor contractor =
                    warehouseContractorService.getContractorById(warehouseId, contractorId, currentUser);

            //model.addAttribute("outbound", false);
            model.addAttribute("orderList", orders);
            model.addAttribute("contractor", contractor);
            model.addAttribute("warehouseId", warehouseId);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute(
                    "statuses",
                    WarehouseOrderStatus.values()
            );

            return "manage-inbound-orders";
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/warehouse/" + warehouseId + "/contractors";
        }
    }

    @PostMapping("/{warehouseId}/contractor-orders/{contractorId}/create")
    public String createOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @PathVariable Long contractorId,
            @ModelAttribute OrderedItemsForm orderedItemsForm,
            @RequestParam boolean outbound,
            @RequestParam(required = false) String additionalInfo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            warehouseOrderService.createOrder(
                    warehouseId,
                    contractorId,
                    orderedItemsForm.getItemRequests(),
                    outbound,
                    additionalInfo,
                    currentUser
            );
            redirectAttributes.addFlashAttribute("success", "Order created successfully");
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return outbound ? "redirect:/warehouse/" + warehouseId + "/contractor-outbound-orders/" + contractorId
                : "redirect:/warehouse/" + warehouseId + "/contractor-inbound-orders/" + contractorId;
    }

    @PostMapping("/{warehouseId}/contractor-orders/{contractorId}/order/{orderId}/change-status")
    public String changeOrderStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @PathVariable Long contractorId,
            @PathVariable Long orderId,
            @RequestParam WarehouseOrderStatus newStatus,
            @RequestParam boolean outbound,
            RedirectAttributes redirectAttributes
    ) {
        try {
            warehouseOrderService.changeOrderStatus(warehouseId, orderId, newStatus, currentUser);
            redirectAttributes.addFlashAttribute("success", "Order status changed successfully");
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return outbound ? "redirect:/warehouse/" + warehouseId + "/contractor-outbound-orders/" + contractorId
                : "redirect:/warehouse/" + warehouseId + "/contractor-inbound-orders/" + contractorId;    }

    @PostMapping("/{warehouseId}/contractor-orders/{contractorId}/order/{orderId}/delete")
    public String deleteOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long warehouseId,
            @PathVariable Long contractorId,
            @PathVariable Long orderId,
            @RequestParam boolean outbound,
            RedirectAttributes redirectAttributes
    ) {
        try {
            warehouseOrderService.deleteOrder(warehouseId, orderId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Order deleted successfully");
        } catch (BusinessValidationException | ResourceAccessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return outbound ? "redirect:/warehouse/" + warehouseId + "/contractor-outbound-orders/" + contractorId
                : "redirect:/warehouse/" + warehouseId + "/contractor-inbound-orders/" + contractorId;    }
}
package me.mmtr.xwarehouse.model.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderedItemsForm {
    private List<OrderItemRequest> itemRequests = new ArrayList<>();

    public List<OrderItemRequest> getItemRequests() {
        return itemRequests;
    }

    public void setItemRequests(List<OrderItemRequest> itemRequests) {
        this.itemRequests = itemRequests;
    }
}

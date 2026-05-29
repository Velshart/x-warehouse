package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.exception.ResourceAccessException;
import me.mmtr.xwarehouse.exception.ResourceNotFoundException;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.model.Warehouse;
import me.mmtr.xwarehouse.model.WarehouseItem;
import me.mmtr.xwarehouse.model.WarehouseZone;
import me.mmtr.xwarehouse.service.WarehouseItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseItemController.class)
public class WarehouseItemControllerTests {

    @MockitoBean
    private WarehouseItemService itemService;

    private User testUser;
    private Warehouse testWarehouse;
    private WarehouseZone testZone;
    private WarehouseItem testItem;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testWarehouse = new Warehouse();
        testWarehouse.setId(2L);
        testWarehouse.setName("Central Warehouse");
        testWarehouse.setOwner(testUser);

        testZone = new WarehouseZone("Zone A", testWarehouse);
        testZone.setId(3L);

        testItem = new WarehouseItem();
        testItem.setId(4L);
        testItem.setName("Item A");
        testItem.setSku("TEST-01");
        testItem.setQuantity(10L);
    }

    @Nested
    class AddItemToZoneTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId() + "/add-item")
                            .param("name", testItem.getName())
                            .param("sku", testItem.getSku())
                            .param(
                                    "quantity",
                                    String.valueOf(testItem.getQuantity())
                            )
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId()))
                    .andExpect(flash().attribute(
                            "success",
                            "Item registered successfully."
                    ));

            verify(itemService, times(1))
                    .addItemToZone(
                            testWarehouse.getId(),
                            testZone.getId(),
                            testItem.getName(),
                            testItem.getSku(),
                            testItem.getQuantity(),
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException(
                    "You are not permitted to perform modifications" +
                            " in this warehouse"
            )).when(itemService).addItemToZone(
                    testWarehouse.getId(),
                    testZone.getId(),
                    testItem.getName(),
                    testItem.getSku(),
                    testItem.getQuantity(),
                    testUser
            );

            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId() + "/add-item")
                            .param("name", testItem.getName())
                            .param("sku", testItem.getSku())
                            .param(
                                    "quantity",
                                    String.valueOf(testItem.getQuantity())
                            )
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId()))
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform modifications" +
                                    " in this warehouse"
                    ));
        }
    }

    @Nested
    class UpdateItemInZoneTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId() + "/update-item")
                            .param(
                                    "itemId",
                                    String.valueOf(testItem.getId())
                            )
                            .param("name", testItem.getName())
                            .param("sku", testItem.getSku())
                            .param(
                                    "quantity",
                                    String.valueOf(testItem.getQuantity())
                            )
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId()))
                    .andExpect(flash().attribute(
                            "success",
                            "Item updated successfully."
                    ));

            verify(itemService, times(1)).updateItemInZone(
                    testWarehouse.getId(),
                    testItem.getId(),
                    testItem.getName(),
                    testItem.getSku(),
                    testItem.getQuantity(),
                    testUser
            );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceNotFoundException("Item does not exist"))
                    .when(itemService).updateItemInZone(
                            testWarehouse.getId(),
                            testItem.getId(),
                            testItem.getName(),
                            testItem.getSku(),
                            testItem.getQuantity(),
                            testUser
                    );

            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId() + "/update-item")
                            .param(
                                    "itemId",
                                    String.valueOf(testItem.getId())
                            )
                            .param("name", testItem.getName())
                            .param("sku", testItem.getSku())
                            .param(
                                    "quantity",
                                    String.valueOf(testItem.getQuantity())
                            )
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId()))
                    .andExpect(flash().attribute(
                            "error",
                            "Item does not exist"
                    ));

        }
    }

    @Nested
    class RemoveItemFromZoneTests {
        @Test
        void verifyIfRedirectionOccursWithSuccessFlashAttribute() throws Exception {
            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId() + "/remove-item")
                            .param(
                                    "itemId",
                                    String.valueOf(testItem.getId())
                            )
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId()))
                    .andExpect(flash().attribute(
                            "success",
                            "Item removed successfully."
                    ));

            verify(itemService, times(1))
                    .removeItemFromZone(
                            testWarehouse.getId(),
                            testItem.getId(),
                            testUser
                    );
        }

        @Test
        void verifyIfRedirectionWithErrorFlashAttributeOccursWhenAnExceptionIsThrown() throws Exception {
            doThrow(new ResourceAccessException("You are not permitted to perform" +
                    " modifications in this warehouse"
            )).when(itemService).removeItemFromZone(
                    testWarehouse.getId(),
                    testItem.getId(),
                    testUser
            );

            mockMvc.perform(post("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId() + "/remove-item")
                            .param(
                                    "itemId",
                                    String.valueOf(testItem.getId())
                            )
                            .with(user(testUser))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/warehouse/" + testWarehouse.getId() + "/zone/" + testZone.getId()))
                    .andExpect(flash().attribute(
                            "error",
                            "You are not permitted to perform modifications" +
                                    " in this warehouse"
                    ));
        }
    }
}

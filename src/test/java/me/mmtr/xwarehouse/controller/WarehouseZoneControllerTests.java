package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.service.WarehouseService;
import me.mmtr.xwarehouse.service.WarehouseZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WarehouseZoneController.class)
public class WarehouseZoneControllerTests {

    @MockitoBean
    private WarehouseZoneService zoneService;

    @MockitoBean
    private WarehouseService warehouseService;

    @Autowired
    private MockMvc mockMvc;
}

# x-warehouse - Warehouse Management System

A project of a WMS web application allowing to effectively manage multiple warehouses.

## Main Features

*   **Warehouse Structure Management:** Warehouses can be divided into multiple zones.
*   **Inventory Control:** Track items, assign unique SKU codes, and monitor stock levels across specific zones.
*   **Order Processing:** Create and process multiple warehouse orders at the same time.
*   **Contractor Management:** Maintain a directory of clients and suppliers.
*   **Team & Access Control:** Assign system users to specific warehouses and manage access using a role hierarchy.

### Backend
*   **Java 21**
*   **Spring Boot 3.x**
    *   Spring Data JPA (Data Access Layer / Hibernate)
    *   Spring Security (Authentication & Authorization)
    *   Spring Validation (Request and Entity validation)
    *   Spring Web (MVC Architecture)
    *   Thymeleaf (Template Engine)

### Database
*   **MySQL**

#### Entity Relationship Diagram
![erd.png](.github/images/erd.png)

## Interface

### Login Page
![login.png](.github/images/login.png)

### Main Dashboard
![dashboard.png](.github/images/dashboard.png)

### Warehouse Management Panel
![manage-warehouse.png](.github/images/manage-warehouse.png)

### Zone Management Panel
![manage-zone.png](.github/images/manage-zone.png)

### Editing of a Stored Item
![manage-zone-edit-stored-item.png](.github/images/manage-zone-edit-stored-item.png)

### Contractors Management Panel
![manage-contractors.png](.github/images/manage-contractors.png)
### Order Management Panel
![manage-inbound-orders.png](.github/images/manage-inbound-orders.png)
![manage-outbound-orders.png](.github/images/manage-outbound-orders.png)

### Changing order status
![manage-orders-change-status.png](.github/images/manage-orders-change-status.png)

### Order Item Summary
![manage-orders-items-summary.png](.github/images/manage-orders-items-summary.png)
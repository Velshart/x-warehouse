package me.mmtr.xwarehouse.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "warehouse_contractors")
public class WarehouseContractor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Contractor code is required")
    @Column(nullable = false, length = 50)
    private String code;

    @NotBlank(message = "Contractor name is required")
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 15)
    private String nip;

    @Column
    private String additionalInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    public WarehouseContractor() {}

    public WarehouseContractor(
            String code,
            String name,
            String nip,
            String additionalInfo,
            Warehouse warehouse
    ) {
        this.code = code;
        this.name = name;
        this.nip = nip;
        this.additionalInfo = additionalInfo;
        this.warehouse = warehouse;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }
}

package com.yms.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modell für die geplante Lieferung.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Delivery {

    @NotBlank(message = "YMS-Referenz ist erforderlich", groups = ValidatedGroup.class)
    @JsonProperty("ymsReference")
    private String ymsReference;

    @NotBlank(message = "Geplantes Lieferdatum ist erforderlich", groups = ValidatedGroup.class)
    @JsonProperty("plannedDeliveryDate")
    private String plannedDeliveryDate;

    @NotBlank(message = "Geplante Lieferzeit ist erforderlich", groups = ValidatedGroup.class)
    @JsonProperty("plannedDeliveryTime")
    private String plannedDeliveryTime;

    @NotBlank(message = "Geplantes Gewicht ist erforderlich", groups = ValidatedGroup.class)
    @JsonProperty("plannedWeightKg")
    private String plannedWeightKg;

    @NotBlank(message = "Zielort ist erforderlich", groups = ValidatedGroup.class)
    @JsonProperty("targetLocation")
    private String targetLocation;

    @NotBlank(message = "Materialtyp ist erforderlich", groups = ValidatedGroup.class)
    @JsonProperty("materialType")
    private String materialType;

    @NotBlank(message = "Lieferant ist erforderlich", groups = ValidatedGroup.class)
    @JsonProperty("supplier")
    private String supplier;

    @JsonProperty("supplierName")
    private String supplierName;

    @NotBlank(message = "isWarehouse ist erforderlich", groups = ValidatedGroup.class)
    @JsonProperty("isWarehouse")
    private String isWarehouse;

    @JsonProperty("warehouseInfo")
    private String warehouseInfo;

    @NotBlank(message = "isExternalWarehouse ist erforderlich", groups = ValidatedGroup.class)
    @JsonProperty("isExternalWarehouse")
    private String isExternalWarehouse;

    @JsonProperty("externalWarehouseInfo")
    private String externalWarehouseInfo;

    @JsonProperty("remark")
    private String remark;

    @JsonProperty("orderNumber")
    private String orderNumber;

    @JsonProperty("orderPosition")
    private String orderPosition;

    /**
     * Marker-Interface für Validierungsgruppe.
     */
    public interface ValidatedGroup {}
}

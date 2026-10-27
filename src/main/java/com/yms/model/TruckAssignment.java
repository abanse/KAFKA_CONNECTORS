package com.yms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vereinfachtes Modell für die LKW-Einweisung.
 * Wird aus Kafka Topic RHW.YMS.TRUCK.ASSIGNMENT empfangen.
 *
 * JSON-Format:
 * {
 *   "ymsReference": "123456789",
 *   "unloadPlace": "Entladeplatz 5"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckAssignment {

    @NotBlank(message = "YMS-Referenz ist erforderlich")
    @JsonProperty("ymsReference")
    private String ymsReference;

    @NotBlank(message = "Entladeplatz ist erforderlich")
    @JsonProperty("unloadPlace")
    private String unloadPlace;
}
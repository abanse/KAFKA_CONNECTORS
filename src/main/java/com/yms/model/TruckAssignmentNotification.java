package com.yms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modell für die LKW-Einweisung an PAARI.
 * Wird über REST API an http://localhost:8081/paari/assignments gesendet.
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
@Builder
public class TruckAssignmentNotification {

    @JsonProperty("ymsReference")
    private String ymsReference;

    @JsonProperty("unloadPlace")
    private String unloadPlace;

    /**
     * Factory-Methode zum Erstellen einer Notification aus einem TruckAssignment.
     * Da beide Klassen jetzt die gleiche Struktur haben, ist das Mapping 1:1.
     */
    public static TruckAssignmentNotification fromTruckAssignment(TruckAssignment assignment) {
        return TruckAssignmentNotification.builder()
                .ymsReference(assignment.getYmsReference())
                .unloadPlace(assignment.getUnloadPlace())
                .build();
    }
}
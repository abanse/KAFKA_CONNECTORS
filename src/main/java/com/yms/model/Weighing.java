package com.yms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Modell für Eingangs- und Ausgangs-Wiegungen..
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Weighing {

    @NotBlank(message = "YMS-Referenz ist erforderlich")
    @JsonProperty("ymsReference")
    private String ymsReference;

    @NotBlank(message = "LKW-Kennzeichen ist erforderlich")
    @JsonProperty("truckLicensePlate")
    private String truckLicensePlate;

    @NotNull(message = "Gewicht ist erforderlich")
    @Positive(message = "Gewicht muss positiv sein")
    @JsonProperty("weightKg")
    private Integer weightKg;

    @NotNull(message = "Registrierungsnummer ist erforderlich")
    @Positive(message = "Registrierungsnummer muss positiv sein")
    @JsonProperty("registrationNumber")
    private Integer registrationNumber;

    @NotNull(message = "Wiegungstyp ist erforderlich")
    @JsonProperty("weighingType")
    private Integer weighingType;  // 1 = Eingang, 2 = Ausgang

    @NotNull(message = "Zeitstempel ist erforderlich")
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant timestamp;
}

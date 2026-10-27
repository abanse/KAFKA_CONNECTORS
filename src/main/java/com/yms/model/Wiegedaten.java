package com.yms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Wägeschein aus der Waage. Wird nach dem Erstellen und nach jeder Änderung übertragen.
 * Solange der Wägeschein im Status "Erstverwogen" ist, sind Zweitgewicht,
 * ZweitgewichtZeitpunkt und Nettogewicht leer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wiegedaten {

    @NotBlank(message = "Waegescheinnummer ist erforderlich")
    @JsonProperty("Waegescheinnummer")
    private String waegescheinnummer;

    @NotNull(message = "ErstgewichtMengeKilogramm ist erforderlich")
    @Positive(message = "ErstgewichtMengeKilogramm muss positiv sein")
    @JsonProperty("ErstgewichtMengeKilogramm")
    private Integer erstgewichtMengeKilogramm;

    @NotNull(message = "ErstgewichtZeitpunkt ist erforderlich")
    @JsonProperty("ErstgewichtZeitpunkt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant erstgewichtZeitpunkt;

    @Positive(message = "ZweitgewichtMengeKilogramm muss positiv sein")
    @JsonProperty("ZweitgewichtMengeKilogramm")
    private Integer zweitgewichtMengeKilogramm;

    @JsonProperty("ZweitgewichtZeitpunkt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant zweitgewichtZeitpunkt;

    @JsonProperty("NettogewichtMengeKilogramm")
    private Integer nettogewichtMengeKilogramm;

    @JsonProperty("Materialnummer")
    private String materialnummer;

    @JsonProperty("Materialbezeichnung")
    private String materialbezeichnung;

    @JsonProperty("Lieferantennummer")
    private String lieferantennummer;

    @JsonProperty("Lieferantenbezeichnung")
    private String lieferantenbezeichnung;

    @JsonProperty("Entladestellennummer")
    private String entladestellennummer;

    @JsonProperty("Entladestellenbezeichnung")
    private String entladestellenbezeichnung;

    @JsonProperty("Fahrzeugkennzeichen")
    private String fahrzeugkennzeichen;

    /**
     * true, sobald die Zweitwägung erfolgt ist.
     */
    @JsonIgnore
    public boolean isZweitverwogen() {
        return zweitgewichtMengeKilogramm != null;
    }

    /**
     * Zweitgewicht, ZweitgewichtZeitpunkt und Nettogewicht sind entweder alle gesetzt oder alle leer.
     */
    @JsonIgnore
    @AssertTrue(message = "ZweitgewichtMengeKilogramm, ZweitgewichtZeitpunkt und NettogewichtMengeKilogramm "
            + "müssen entweder alle gesetzt oder alle leer sein")
    public boolean isZweitwaegungVollstaendig() {
        boolean gewicht = zweitgewichtMengeKilogramm != null;
        return gewicht == (zweitgewichtZeitpunkt != null)
                && gewicht == (nettogewichtMengeKilogramm != null);
    }
}

package com.yms.controller;

import com.yms.model.Wiegedaten;
import com.yms.service.DeliveryValidationService;
import com.yms.service.KafkaProducerService;
import com.yms.service.YmsRestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(YmsController.class)
class WiegedatenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @MockBean
    private YmsRestClient ymsRestClient;

    @MockBean
    private DeliveryValidationService deliveryValidationService;

    private static final String ZWEITVERWOGEN = """
            {
              "Waegescheinnummer": "WS-2026-89412",
              "ErstgewichtMengeKilogramm": 14200,
              "ErstgewichtZeitpunkt": "2026-10-22T13:15:00Z",
              "ZweitgewichtMengeKilogramm": 4100,
              "ZweitgewichtZeitpunkt": "2026-10-22T13:45:00Z",
              "NettogewichtMengeKilogramm": 10100,
              "Materialnummer": "MAT-9982",
              "Materialbezeichnung": "Kupferschrott schwer",
              "Lieferantennummer": "LIEF-4401",
              "Lieferantenbezeichnung": "Metallhandel Müller GmbH",
              "Entladestellennummer": "EL-02",
              "Entladestellenbezeichnung": "Halle 4 Nord",
              "Fahrzeugkennzeichen": "EF-AB-1234"
            }
            """;

    @Test
    @DisplayName("POST /wiegedaten: Beispiel aus der Spezifikation wird angenommen und an Kafka gesendet")
    void zweitverwogen_wirdAngenommen() throws Exception {
        mockMvc.perform(post("/api/yms/wiegedaten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ZWEITVERWOGEN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.waegestatus").value("ZWEITVERWOGEN"));

        ArgumentCaptor<Wiegedaten> captor = ArgumentCaptor.forClass(Wiegedaten.class);
        verify(kafkaProducerService).sendWiegedaten(captor.capture());
        Wiegedaten w = captor.getValue();
        assertThat(w.getWaegescheinnummer()).isEqualTo("WS-2026-89412");
        assertThat(w.getErstgewichtMengeKilogramm()).isEqualTo(14200);
        assertThat(w.getErstgewichtZeitpunkt()).isEqualTo(Instant.parse("2026-10-22T13:15:00Z"));
        assertThat(w.getNettogewichtMengeKilogramm()).isEqualTo(10100);
        assertThat(w.getLieferantenbezeichnung()).isEqualTo("Metallhandel Müller GmbH");
        assertThat(w.getFahrzeugkennzeichen()).isEqualTo("EF-AB-1234");
    }

    @Test
    @DisplayName("POST /wiegedaten: Erstverwogen mit leeren Zweitwägungsfeldern wird angenommen")
    void erstverwogen_wirdAngenommen() throws Exception {
        String json = """
                {
                  "Waegescheinnummer": "WS-2026-89412",
                  "ErstgewichtMengeKilogramm": 14200,
                  "ErstgewichtZeitpunkt": "2026-10-22T13:15:00Z",
                  "ZweitgewichtMengeKilogramm": null,
                  "ZweitgewichtZeitpunkt": "",
                  "Fahrzeugkennzeichen": "EF-AB-1234"
                }
                """;

        mockMvc.perform(post("/api/yms/wiegedaten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.waegestatus").value("ERSTVERWOGEN"));

        verify(kafkaProducerService).sendWiegedaten(any());
    }

    @Test
    @DisplayName("POST /wiegedaten: fehlende Waegescheinnummer wird abgelehnt")
    void ohneWaegescheinnummer_wirdAbgelehnt() throws Exception {
        String json = ZWEITVERWOGEN.replace("\"Waegescheinnummer\": \"WS-2026-89412\",", "");

        mockMvc.perform(post("/api/yms/wiegedaten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(kafkaProducerService, never()).sendWiegedaten(any());
    }

    @Test
    @DisplayName("POST /wiegedaten: Zweitgewicht ohne Nettogewicht wird abgelehnt")
    void unvollstaendigeZweitwaegung_wirdAbgelehnt() throws Exception {
        String json = ZWEITVERWOGEN.replace("\"NettogewichtMengeKilogramm\": 10100,", "");

        mockMvc.perform(post("/api/yms/wiegedaten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(kafkaProducerService, never()).sendWiegedaten(any());
    }
}

package com.yms.service;

import com.yms.model.TruckAssignmentNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

/**
 * REST Client Service für die Kommunikation mit dem PAARI System.
 * Sendet vereinfachte LKW-Einweisungen an PAARI über deren REST API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YmsRestClient {

    private final RestTemplate restTemplate;

    @Value("${yms.api.base-url}")
    private String ymsBaseUrl;

    @Value("${yms.api.truck-assignment-endpoint}")
    private String truckAssignmentEndpoint;

    @Value("${yms.api.auth.enabled:false}")
    private boolean authEnabled;

    @Value("${yms.api.auth.token:}")
    private String authToken;

    @Value("${yms.api.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${yms.api.retry.delay-ms:1000}")
    private long retryDelayMs;

    /**
     * Sendet eine vereinfachte LKW-Einweisung an PAARI
     * JSON-Format: {"ymsReference": "...", "unloadPlace": "...", "box": "..."}
     *
     * @param notification Die zu sendende Einweisungs-Benachrichtigung
     * @return true wenn erfolgreich, false bei Fehler
     */
    public boolean sendTruckAssignment(TruckAssignmentNotification notification) {
        String url = ymsBaseUrl + truckAssignmentEndpoint;
        
        log.info("Sende LKW-Einweisung an PAARI: URL={}", url);
        log.info("JSON-Payload: ymsReference={}, unloadPlace={}, box={}", 
                notification.getYmsReference(),
                notification.getUnloadPlace()
        //        notification.getBox()
        );

        // Retry-Logik für Netzwerkfehler
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                ResponseEntity<Map> response = sendRequest(url, notification);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("LKW-Einweisung erfolgreich an PAARI übertragen. " +
                            "Status: {}, YMS-Ref: {}, Response: {}",
                            response.getStatusCode(),
                            notification.getYmsReference(),
                            response.getBody());
                    return true;
                } else {
                    log.error("Unerwarteter Status von PAARI: {}, YMS-Ref: {}",
                            response.getStatusCode(), notification.getYmsReference());
                    return false;
                }

            } catch (HttpClientErrorException e) {
                // 4xx Fehler - Client-Fehler, kein Retry
                log.error("Client-Fehler beim Senden an PAARI (4xx): Status={}, YMS-Ref={}, Body={}",
                        e.getStatusCode(), notification.getYmsReference(), e.getResponseBodyAsString());
                return false;
                
            } catch (HttpServerErrorException e) {
                // 5xx Fehler - Server-Fehler, Retry möglich
                log.error("Server-Fehler beim Senden an PAARI (5xx) - Versuch {}/{}: Status={}, YMS-Ref={}",
                        attempt, maxRetryAttempts, e.getStatusCode(), notification.getYmsReference());
                
                if (attempt < maxRetryAttempts) {
                    sleep(retryDelayMs * attempt); // Exponentielles Backoff
                    continue;
                }
                return false;
                
            } catch (ResourceAccessException e) {
                // Netzwerkfehler, Timeout etc.
                log.error("Netzwerkfehler beim Senden an PAARI - Versuch {}/{}: YMS-Ref={}, Fehler={}",
                        attempt, maxRetryAttempts, notification.getYmsReference(), e.getMessage());
                
                if (attempt < maxRetryAttempts) {
                    sleep(retryDelayMs * attempt);
                    continue;
                }
                return false;
                
            } catch (Exception e) {
                log.error("Unerwarteter Fehler beim Senden an PAARI: YMS-Ref={}",
                        notification.getYmsReference(), e);
                return false;
            }
        }

        log.error("Maximale Anzahl von Versuchen erreicht für YMS-Ref: {}",
                notification.getYmsReference());
        return false;
    }

    /**
     * Sendet die HTTP-Anfrage an PAARI
     */
    private ResponseEntity<Map> sendRequest(String url, TruckAssignmentNotification notification) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Füge Auth-Token hinzu, falls konfiguriert
        if (authEnabled && authToken != null && !authToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + authToken);
        }
        
        // Zusätzliche Header für Tracking
        headers.set("X-YMS-Reference", notification.getYmsReference());
        headers.set("X-Source-System", "KAFKA-BRIDGE");
        
        HttpEntity<TruckAssignmentNotification> request = new HttpEntity<>(notification, headers);
        
        log.debug("Sende POST Request an: {}", url);
        
        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );
    }

    /**
     * Health-Check für PAARI API
     */
    public boolean checkYmsHealth() {
        try {
            String healthUrl = ymsBaseUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            boolean healthy = response.getStatusCode().is2xxSuccessful();
            
            log.info("PAARI Health-Check: Status={}, Healthy={}", 
                    response.getStatusCode(), healthy);
            return healthy;
            
        } catch (Exception e) {
            log.warn("PAARI Health-Check fehlgeschlagen: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Hilfsmethode für Thread-Sleep bei Retries
     */
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread wurde während des Wartens unterbrochen");
        }
    }
}
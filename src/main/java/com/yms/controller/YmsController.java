package com.yms.controller;

import com.yms.model.Delivery;
import com.yms.model.TruckAssignment;
import com.yms.model.TruckAssignmentNotification;
import com.yms.model.Weighing;
import com.yms.service.DeliveryValidationService;
import com.yms.service.KafkaProducerService;
import com.yms.service.YmsRestClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller für YMS Datenerfassung.
 *
 * LKW-Einweisung JSON-Format:
 * {
 *   "ymsReference": "123456789",
 *   "unloadPlace": "Entladeplatz 5"
 * }
 */
@RestController
@RequestMapping("/api/yms")
@RequiredArgsConstructor
@Slf4j
public class YmsController {

    private final KafkaProducerService kafkaProducerService;
    private final YmsRestClient ymsRestClient;
    private final DeliveryValidationService deliveryValidationService;

    /**
     * POST /api/yms/delivery - Erstellt eine neue Lieferung
     */
    @PostMapping("/delivery")
    public ResponseEntity<Map<String, Object>> createDelivery(@RequestBody Delivery delivery) {
        log.info("Empfange neue Lieferung: YMS-Ref={}", delivery.getYmsReference());

        // Konfigurierbare Validierung
        List<String> validationErrors = deliveryValidationService.validate(delivery);
        if (!validationErrors.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Validierungsfehler");
            errorResponse.put("errors", validationErrors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            kafkaProducerService.sendDelivery(delivery);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Lieferung erfolgreich an Kafka gesendet");
            response.put("ymsReference", delivery.getYmsReference());
            response.put("topic", "yms.delivery");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten der Lieferung", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Fehler beim Senden an Kafka: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/yms/weighing - Erstellt eine Wiegung (Eingang oder Ausgang)
     */
    @PostMapping("/weighing")
    public ResponseEntity<Map<String, Object>> createWeighing(@Valid @RequestBody Weighing weighing) {
        String type = weighing.getWeighingType() == 1 ? "Eingangs" : "Ausgangs";
        log.info("Empfange neue {}-Wiegung: YMS-Ref={}, Gewicht={} kg", 
                type, weighing.getYmsReference(), weighing.getWeightKg());
        
        try {
            if (weighing.getWeighingType() != 1 && weighing.getWeighingType() != 2) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Ungültiger Wiegungstyp. Erlaubt sind: 1 (Eingang) oder 2 (Ausgang)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            kafkaProducerService.sendWeighing(weighing);
            
            String topic = weighing.getWeighingType() == 1 ? "yms.weighing.in" : "yms.weighing.out";
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", type + "-Wiegung erfolgreich an Kafka gesendet");
            response.put("ymsReference", weighing.getYmsReference());
            response.put("weighingType", weighing.getWeighingType());
            response.put("weightKg", weighing.getWeightKg());
            response.put("topic", topic);
            
            if (weighing.getWeighingType() == 1) {
                response.put("info", "LKW-Einweisung wird nach Verarbeitung über Kafka empfangen");
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten der Wiegung", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Fehler beim Senden an Kafka: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/yms/truck-assignment/manual - Manuelle LKW-Einweisung
     * Sendet direkt an PAARI: http://localhost:8081/paari/assignments
     *
     * Erwartet JSON:
     * {
     *   "ymsReference": "123456789",
     *   "unloadPlace": "Entladeplatz 5"
     * }
     */
    @PostMapping("/truck-assignment/manual")
    public ResponseEntity<Map<String, Object>> createManualTruckAssignment(@Valid @RequestBody TruckAssignment assignment) {
        log.info("Empfange manuelle LKW-Einweisung: ymsReference={}, unloadPlace={}",
                assignment.getYmsReference(),
                assignment.getUnloadPlace());
        
        try {
            // Erstelle Notification für PAARI
            TruckAssignmentNotification notification = 
                    TruckAssignmentNotification.fromTruckAssignment(assignment);
            
            boolean success = ymsRestClient.sendTruckAssignment(notification);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "LKW-Einweisung erfolgreich an PAARI gesendet");
                response.put("ymsReference", notification.getYmsReference());
                response.put("unloadPlace", notification.getUnloadPlace());
                response.put("targetUrl", "http://localhost:8081/paari/assignments");
                response.put("type", "MANUAL");
                
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Fehler beim Senden der LKW-Einweisung an PAARI");
                
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }
            
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten der manuellen LKW-Einweisung", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Fehler beim Verarbeiten: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/yms/truck-assignment - Sendet eine LKW-Einweisung an Kafka
     *
     * Erwartet und sendet JSON:
     * {
     *   "ymsReference": "123456789",
     *   "unloadPlace": "Entladeplatz 5"
     * }
     */
    @PostMapping("/truck-assignment")
    public ResponseEntity<Map<String, Object>> sendTruckAssignmentToKafka(@Valid @RequestBody TruckAssignment assignment) {
        log.info("Sende LKW-Einweisung an Kafka: ymsReference={}, unloadPlace={}",
                assignment.getYmsReference(),
                assignment.getUnloadPlace());
        
        try {
            kafkaProducerService.sendTruckAssignment(assignment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "LKW-Einweisung erfolgreich an Kafka gesendet");
            response.put("ymsReference", assignment.getYmsReference());
            response.put("unloadPlace", assignment.getUnloadPlace());
            response.put("topic", "RHW.YMS.TRUCK.ASSIGNMENT");
            response.put("info", "Einweisung wird automatisch an PAARI weitergeleitet");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Fehler beim Senden der LKW-Einweisung an Kafka", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Fehler beim Senden an Kafka: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/yms/health - Health Check Endpunkt
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "YMS Kafka API");
        response.put("version", "1.2.0");
        response.put("features", new String[]{
            "delivery", 
            "weighing-in", 
            "weighing-out", 
            "truck-assignment"
        });
        
        boolean paariHealthy = ymsRestClient.checkYmsHealth();
        response.put("paariConnection", paariHealthy ? "UP" : "DOWN");
        response.put("paariEndpoint", "http://localhost:8081/paari/assignments");
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/yms/config - Zeigt aktuelle Konfiguration
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        
        Map<String, String> topics = new HashMap<>();
        topics.put("delivery", "RHW.YMS.DELIVERY");
        topics.put("weighing-in", "RHW.YMS.WEIGHING.IN");
        topics.put("weighing-out", "RHW.YMS.WEIGHING.OUT");
        topics.put("truck-assignment", "RHW.YMS.TRUCK.ASSIGNMENT");
        
        config.put("kafkaTopics", topics);
        config.put("consumerGroupId", "yms-truck-assignment-consumer");
        config.put("paariEndpoint", "http://localhost:8081/paari/assignments");
        
        // Zeige das JSON-Format
        Map<String, String> jsonFormat = new HashMap<>();
        jsonFormat.put("ymsReference", "String (Pflicht)");
        jsonFormat.put("unloadPlace", "String (Pflicht)");
        config.put("truckAssignmentJsonFormat", jsonFormat);
        
        return ResponseEntity.ok(config);
    }
}
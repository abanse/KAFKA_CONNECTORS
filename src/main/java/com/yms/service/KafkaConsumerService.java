package com.yms.service;

import com.yms.model.TruckAssignment;
import com.yms.model.TruckAssignmentNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer Service für das Empfangen von LKW-Einweisungen.
 * 
 * Empfängt aus Topic RHW.YMS.TRUCK.ASSIGNMENT:
 * {
 *   "ymsReference": "123456789",
 *   "unloadPlace": "Entladeplatz 5",
 *   "box": "B-12"
 * }
 * 
 * Leitet an PAARI weiter: http://localhost:8081/paari/assignments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final YmsRestClient ymsRestClient;

    /**
     * Empfängt LKW-Einweisungen aus Kafka und leitet sie an PAARI weiter.
     */
    @KafkaListener(
            topics = "${kafka.topics.truck-assignment}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void receiveTruckAssignment(
            @Payload TruckAssignment assignment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Empfange LKW-Einweisung aus Kafka - Topic: {}, Partition: {}, Offset: {}",
                topic, partition, offset);
        
        log.info("Kafka-Nachricht: ymsReference={}, unloadPlace={}, box={}",
                assignment.getYmsReference(),
                assignment.getUnloadPlace()
                //assignment.getBox()
                );

        try {
            // Validierung der empfangenen Nachricht
            validateAssignment(assignment);

            // Erstelle Notification für PAARI (1:1 Mapping)
            TruckAssignmentNotification notification = 
                    TruckAssignmentNotification.fromTruckAssignment(assignment);

            log.info("Sende an PAARI: ymsReference={}, unloadPlace={}, box={}",
                    notification.getYmsReference(),
                    notification.getUnloadPlace()
                    //assignment.getBox()
            );

            // Sende an PAARI über REST API
            boolean success = ymsRestClient.sendTruckAssignment(notification);

            if (success) {
                log.info("LKW-Einweisung erfolgreich an PAARI gesendet: ymsReference={}", 
                        assignment.getYmsReference());
                // Bestätige die Nachricht
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
            } else {
                log.error("Fehler beim Senden der LKW-Einweisung an PAARI: ymsReference={}", 
                        assignment.getYmsReference());
                handleProcessingError(assignment, new RuntimeException("PAARI nicht erreichbar"));
            }

        } catch (Exception e) {
            log.error("Fehler bei der Verarbeitung der LKW-Einweisung: ymsReference={}", 
                    assignment.getYmsReference(), e);
            handleProcessingError(assignment, e);
        }
    }

    /**
     * Validiert die empfangene LKW-Einweisung
     */
    private void validateAssignment(TruckAssignment assignment) {
        if (assignment.getYmsReference() == null || assignment.getYmsReference().isEmpty()) {
            throw new IllegalArgumentException("ymsReference fehlt");
        }
        if (assignment.getUnloadPlace() == null || assignment.getUnloadPlace().isEmpty()) {
            throw new IllegalArgumentException("unloadPlace fehlt");
        }
        // Box ist optional, aber logge Warnung wenn leer
       // if (assignment.getBox() == null || assignment.getBox().isEmpty()) {
      //      log.warn("box nicht angegeben für ymsReference: {}", assignment.getYmsReference());
      //  }
    }

    /**
     * Behandelt Fehler bei der Verarbeitung
     */
    private void handleProcessingError(TruckAssignment assignment, Exception e) {
        log.error("Error-Handling für ymsReference={} - Fehler: {}", 
                assignment.getYmsReference(), e.getMessage());
    }
}
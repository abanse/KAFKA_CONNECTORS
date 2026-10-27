package com.yms.service;

import com.yms.model.Delivery;
import com.yms.model.TruckAssignment;
import com.yms.model.Weighing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service für das Senden von Nachrichten an Kafka.
 * 
 * LKW-Einweisung JSON-Format:
 * {
 *   "ymsReference": "123456789",
 *   "unloadPlace": "Entladeplatz 5",
 *   "box": "B-12"
 * }
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.delivery}")
    private String deliveryTopic;

    @Value("${kafka.topics.weighing-in}")
    private String weighingInTopic;

    @Value("${kafka.topics.weighing-out}")
    private String weighingOutTopic;

    @Value("${kafka.topics.truck-assignment}")
    private String truckAssignmentTopic;

    /**
     * Sendet eine Lieferung an Kafka
     */
    public CompletableFuture<SendResult<String, Object>> sendDelivery(Delivery delivery) {
        log.info("Sende Lieferung an Kafka - YMS-Ref: {}, Topic: {}", 
                delivery.getYmsReference(), deliveryTopic);
        
        return kafkaTemplate.send(deliveryTopic, delivery.getYmsReference(), delivery)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Lieferung erfolgreich gesendet: YMS-Ref={}, Offset={}", 
                                delivery.getYmsReference(), 
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Fehler beim Senden der Lieferung: YMS-Ref={}", 
                                delivery.getYmsReference(), ex);
                    }
                });
    }

    /**
     * Sendet eine Wiegung an Kafka (Eingang oder Ausgang basierend auf weighingType)
     */
    public CompletableFuture<SendResult<String, Object>> sendWeighing(Weighing weighing) {
        String topic = weighing.getWeighingType() == 1 ? weighingInTopic : weighingOutTopic;
        String type = weighing.getWeighingType() == 1 ? "Eingang" : "Ausgang";
        
        log.info("Sende {}-Wiegung an Kafka - YMS-Ref: {}, Gewicht: {} kg, Topic: {}", 
                type, weighing.getYmsReference(), weighing.getWeightKg(), topic);
        
        return kafkaTemplate.send(topic, weighing.getYmsReference(), weighing)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("{}-Wiegung erfolgreich gesendet: YMS-Ref={}, Offset={}, Partition={}", 
                                type, 
                                weighing.getYmsReference(), 
                                result.getRecordMetadata().offset(),
                                result.getRecordMetadata().partition());
                    } else {
                        log.error("Fehler beim Senden der {}-Wiegung: YMS-Ref={}", 
                                type, weighing.getYmsReference(), ex);
                    }
                });
    }

    /**
     * Sendet eine LKW-Einweisung an Kafka
     * 
     * JSON-Format:
     * {
     *   "ymsReference": "123456789",
     *   "unloadPlace": "Entladeplatz 5",
     *   "box": "B-12"
     * }
     */
    public CompletableFuture<SendResult<String, Object>> sendTruckAssignment(TruckAssignment assignment) {
        log.info("Sende LKW-Einweisung an Kafka - ymsReference: {}, unloadPlace: {}, box: {}, Topic: {}", 
                assignment.getYmsReference(), 
                assignment.getUnloadPlace(),
               // assignment.getBox(),
                truckAssignmentTopic);
        
        return kafkaTemplate.send(truckAssignmentTopic, assignment.getYmsReference(), assignment)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("LKW-Einweisung erfolgreich an Kafka gesendet: " +
                                "ymsReference={}, Offset={}, Partition={}", 
                                assignment.getYmsReference(), 
                                result.getRecordMetadata().offset(),
                                result.getRecordMetadata().partition());
                    } else {
                        log.error("Fehler beim Senden der LKW-Einweisung an Kafka: ymsReference={}", 
                                assignment.getYmsReference(), ex);
                    }
                });
    }
}
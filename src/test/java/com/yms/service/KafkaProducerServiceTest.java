package com.yms.service;

import com.yms.model.Delivery;
import com.yms.model.TruckAssignment;
import com.yms.model.Weighing;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<Object> valueCaptor;

    private KafkaProducerService kafkaProducerService;

    private static final String DELIVERY_TOPIC = "test.delivery.topic";
    private static final String WEIGHING_IN_TOPIC = "test.weighing.in.topic";
    private static final String WEIGHING_OUT_TOPIC = "test.weighing.out.topic";
    private static final String TRUCK_ASSIGNMENT_TOPIC = "test.truck.assignment.topic";

    @BeforeEach
    void setUp() {
        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
        ReflectionTestUtils.setField(kafkaProducerService, "deliveryTopic", DELIVERY_TOPIC);
        ReflectionTestUtils.setField(kafkaProducerService, "weighingInTopic", WEIGHING_IN_TOPIC);
        ReflectionTestUtils.setField(kafkaProducerService, "weighingOutTopic", WEIGHING_OUT_TOPIC);
        ReflectionTestUtils.setField(kafkaProducerService, "truckAssignmentTopic", TRUCK_ASSIGNMENT_TOPIC);
    }

    private CompletableFuture<SendResult<String, Object>> createSuccessFuture(String topic) {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(topic, 0), 0L, 0, 0L, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(
                new ProducerRecord<>(topic, "key", "value"), metadata);
        return CompletableFuture.completedFuture(sendResult);
    }

    // ============== sendDelivery Tests ==============

    @Test
    @DisplayName("sendDelivery: ymsReference wird als Key gesendet")
    void sendDelivery_shouldUseYmsReferenceAsKey() {
        // Given
        String expectedKey = "YMS-REF-12345";
        Delivery delivery = new Delivery();
        delivery.setYmsReference(expectedKey);
        delivery.setPlannedDeliveryDate("2024-01-15");
        delivery.setPlannedDeliveryTime("10:00");
        delivery.setPlannedWeightKg("5000");
        delivery.setTargetLocation("Lager A");
        delivery.setMaterialType("Stahl");
        delivery.setSupplier("Lieferant XY");
        delivery.setIsWarehouse("true");
        delivery.setIsExternalWarehouse("false");

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(createSuccessFuture(DELIVERY_TOPIC));

        // When
        kafkaProducerService.sendDelivery(delivery);

        // Then
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertThat(keyCaptor.getValue())
                .as("Der Kafka-Key muss die ymsReference sein")
                .isEqualTo(expectedKey);
        assertThat(topicCaptor.getValue()).isEqualTo(DELIVERY_TOPIC);
        assertThat(valueCaptor.getValue()).isEqualTo(delivery);
    }

    @Test
    @DisplayName("sendDelivery: Key darf nicht null sein wenn ymsReference gesetzt ist")
    void sendDelivery_keyMustNotBeNullWhenYmsReferenceIsSet() {
        // Given
        Delivery delivery = new Delivery();
        delivery.setYmsReference("REF-NOT-NULL");
        delivery.setPlannedDeliveryDate("2024-01-15");
        delivery.setPlannedDeliveryTime("10:00");
        delivery.setPlannedWeightKg("5000");
        delivery.setTargetLocation("Lager A");
        delivery.setMaterialType("Stahl");
        delivery.setSupplier("Lieferant XY");
        delivery.setIsWarehouse("true");
        delivery.setIsExternalWarehouse("false");

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(createSuccessFuture(DELIVERY_TOPIC));

        // When
        kafkaProducerService.sendDelivery(delivery);

        // Then
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue())
                .as("Der Key darf nicht null sein")
                .isNotNull()
                .isNotEmpty();
    }

    // ============== sendWeighing Tests (Eingang) ==============

    @Test
    @DisplayName("sendWeighing (Eingang): ymsReference wird als Key gesendet")
    void sendWeighingIn_shouldUseYmsReferenceAsKey() {
        // Given
        String expectedKey = "YMS-WEIGH-IN-789";
        Weighing weighing = new Weighing();
        weighing.setYmsReference(expectedKey);
        weighing.setTruckLicensePlate("AB-CD-1234");
        weighing.setWeightKg(15000);
        weighing.setRegistrationNumber(12345);
        weighing.setWeighingType(1); // Eingang
        weighing.setTimestamp(Instant.now());

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(createSuccessFuture(WEIGHING_IN_TOPIC));

        // When
        kafkaProducerService.sendWeighing(weighing);

        // Then
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertThat(keyCaptor.getValue())
                .as("Der Kafka-Key muss die ymsReference sein")
                .isEqualTo(expectedKey);
        assertThat(topicCaptor.getValue())
                .as("Eingangs-Wiegung muss an weighingInTopic gesendet werden")
                .isEqualTo(WEIGHING_IN_TOPIC);
    }

    // ============== sendWeighing Tests (Ausgang) ==============

    @Test
    @DisplayName("sendWeighing (Ausgang): ymsReference wird als Key gesendet")
    void sendWeighingOut_shouldUseYmsReferenceAsKey() {
        // Given
        String expectedKey = "YMS-WEIGH-OUT-456";
        Weighing weighing = new Weighing();
        weighing.setYmsReference(expectedKey);
        weighing.setTruckLicensePlate("XY-ZZ-9999");
        weighing.setWeightKg(8000);
        weighing.setRegistrationNumber(67890);
        weighing.setWeighingType(2); // Ausgang
        weighing.setTimestamp(Instant.now());

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(createSuccessFuture(WEIGHING_OUT_TOPIC));

        // When
        kafkaProducerService.sendWeighing(weighing);

        // Then
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertThat(keyCaptor.getValue())
                .as("Der Kafka-Key muss die ymsReference sein")
                .isEqualTo(expectedKey);
        assertThat(topicCaptor.getValue())
                .as("Ausgangs-Wiegung muss an weighingOutTopic gesendet werden")
                .isEqualTo(WEIGHING_OUT_TOPIC);
    }

    @Test
    @DisplayName("sendWeighing: Key darf nicht null sein wenn ymsReference gesetzt ist")
    void sendWeighing_keyMustNotBeNullWhenYmsReferenceIsSet() {
        // Given
        Weighing weighing = new Weighing();
        weighing.setYmsReference("REF-WEIGH-NOT-NULL");
        weighing.setTruckLicensePlate("AB-CD-1234");
        weighing.setWeightKg(15000);
        weighing.setRegistrationNumber(12345);
        weighing.setWeighingType(1);
        weighing.setTimestamp(Instant.now());

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(createSuccessFuture(WEIGHING_IN_TOPIC));

        // When
        kafkaProducerService.sendWeighing(weighing);

        // Then
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue())
                .as("Der Key darf nicht null sein")
                .isNotNull()
                .isNotEmpty();
    }

    // ============== sendTruckAssignment Tests ==============

    @Test
    @DisplayName("sendTruckAssignment: ymsReference wird als Key gesendet")
    void sendTruckAssignment_shouldUseYmsReferenceAsKey() {
        // Given
        String expectedKey = "YMS-TRUCK-ASSIGN-999";
        TruckAssignment assignment = new TruckAssignment();
        assignment.setYmsReference(expectedKey);
        assignment.setUnloadPlace("Entladeplatz 7");

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(createSuccessFuture(TRUCK_ASSIGNMENT_TOPIC));

        // When
        kafkaProducerService.sendTruckAssignment(assignment);

        // Then
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertThat(keyCaptor.getValue())
                .as("Der Kafka-Key muss die ymsReference sein")
                .isEqualTo(expectedKey);
        assertThat(topicCaptor.getValue()).isEqualTo(TRUCK_ASSIGNMENT_TOPIC);
        assertThat(valueCaptor.getValue()).isEqualTo(assignment);
    }

    @Test
    @DisplayName("sendTruckAssignment: Key darf nicht null sein wenn ymsReference gesetzt ist")
    void sendTruckAssignment_keyMustNotBeNullWhenYmsReferenceIsSet() {
        // Given
        TruckAssignment assignment = new TruckAssignment();
        assignment.setYmsReference("REF-TRUCK-NOT-NULL");
        assignment.setUnloadPlace("Entladeplatz 3");

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(createSuccessFuture(TRUCK_ASSIGNMENT_TOPIC));

        // When
        kafkaProducerService.sendTruckAssignment(assignment);

        // Then
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue())
                .as("Der Key darf nicht null sein")
                .isNotNull()
                .isNotEmpty();
    }

    // ============== Alle Methoden - Konsistenz-Test ==============

    @Test
    @DisplayName("Alle send-Methoden verwenden ymsReference als Key konsistent")
    void allSendMethods_shouldUseYmsReferenceAsKeyConsistently() {
        // Given
        String deliveryRef = "DELIVERY-KEY-001";
        String weighingRef = "WEIGHING-KEY-002";
        String truckRef = "TRUCK-KEY-003";

        Delivery delivery = new Delivery();
        delivery.setYmsReference(deliveryRef);
        delivery.setPlannedDeliveryDate("2024-01-15");
        delivery.setPlannedDeliveryTime("10:00");
        delivery.setPlannedWeightKg("5000");
        delivery.setTargetLocation("Lager A");
        delivery.setMaterialType("Stahl");
        delivery.setSupplier("Lieferant XY");
        delivery.setIsWarehouse("true");
        delivery.setIsExternalWarehouse("false");

        Weighing weighing = new Weighing();
        weighing.setYmsReference(weighingRef);
        weighing.setTruckLicensePlate("AB-CD-1234");
        weighing.setWeightKg(15000);
        weighing.setRegistrationNumber(12345);
        weighing.setWeighingType(1);
        weighing.setTimestamp(Instant.now());

        TruckAssignment assignment = new TruckAssignment();
        assignment.setYmsReference(truckRef);
        assignment.setUnloadPlace("Entladeplatz 5");

        when(kafkaTemplate.send(eq(DELIVERY_TOPIC), anyString(), any()))
                .thenReturn(createSuccessFuture(DELIVERY_TOPIC));
        when(kafkaTemplate.send(eq(WEIGHING_IN_TOPIC), anyString(), any()))
                .thenReturn(createSuccessFuture(WEIGHING_IN_TOPIC));
        when(kafkaTemplate.send(eq(TRUCK_ASSIGNMENT_TOPIC), anyString(), any()))
                .thenReturn(createSuccessFuture(TRUCK_ASSIGNMENT_TOPIC));

        // When
        kafkaProducerService.sendDelivery(delivery);
        kafkaProducerService.sendWeighing(weighing);
        kafkaProducerService.sendTruckAssignment(assignment);

        // Then
        verify(kafkaTemplate).send(eq(DELIVERY_TOPIC), eq(deliveryRef), eq(delivery));
        verify(kafkaTemplate).send(eq(WEIGHING_IN_TOPIC), eq(weighingRef), eq(weighing));
        verify(kafkaTemplate).send(eq(TRUCK_ASSIGNMENT_TOPIC), eq(truckRef), eq(assignment));
    }
}

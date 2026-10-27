package com.yms.service;

import com.yms.config.ValidationConfig;
import com.yms.model.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service für konfigurierbare Delivery-Validierung   .
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryValidationService {

    private final ValidationConfig validationConfig;

    /**
     * Validiert eine Delivery basierend auf der Konfiguration.
     * @return Liste der Validierungsfehler (leer wenn keine Fehler)
     */
    public List<String> validate(Delivery delivery) {
        List<String> errors = new ArrayList<>();

        if (!validationConfig.isValidationEnabled()) {
            log.debug("Validierung ist global deaktiviert");
            return errors;
        }

        if (validationConfig.isFieldValidationEnabled("ymsReference")) {
            if (isBlank(delivery.getYmsReference())) {
                errors.add("YMS-Referenz ist erforderlich");
            }
        }

        if (validationConfig.isFieldValidationEnabled("plannedDeliveryDate")) {
            if (isBlank(delivery.getPlannedDeliveryDate())) {
                errors.add("Geplantes Lieferdatum ist erforderlich");
            }
        }

        if (validationConfig.isFieldValidationEnabled("plannedDeliveryTime")) {
            if (isBlank(delivery.getPlannedDeliveryTime())) {
                errors.add("Geplante Lieferzeit ist erforderlich");
            }
        }

        if (validationConfig.isFieldValidationEnabled("plannedWeightKg")) {
            if (isBlank(delivery.getPlannedWeightKg())) {
                errors.add("Geplantes Gewicht ist erforderlich");
            }
        }

        if (validationConfig.isFieldValidationEnabled("targetLocation")) {
            if (isBlank(delivery.getTargetLocation())) {
                errors.add("Zielort ist erforderlich");
            }
        }

        if (validationConfig.isFieldValidationEnabled("materialType")) {
            if (isBlank(delivery.getMaterialType())) {
                errors.add("Materialtyp ist erforderlich");
            }
        }

        if (validationConfig.isFieldValidationEnabled("supplier")) {
            if (isBlank(delivery.getSupplier())) {
                errors.add("Lieferant ist erforderlich");
            }
        }

        if (validationConfig.isFieldValidationEnabled("isWarehouse")) {
            if (isBlank(delivery.getIsWarehouse())) {
                errors.add("isWarehouse ist erforderlich");
            }
        }

        if (validationConfig.isFieldValidationEnabled("isExternalWarehouse")) {
            if (isBlank(delivery.getIsExternalWarehouse())) {
                errors.add("isExternalWarehouse ist erforderlich");
            }
        }

        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package com.yms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Konfiguration für ein-/ausschaltbare Feldvalidierung. und Einzelfelder
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "yms.validation")
public class ValidationConfig {

    private boolean enabled = true;
    private Map<String, Boolean> delivery = new HashMap<>();

    /**
     * Prüft ob die Validierung für ein bestimmtes Feld aktiviert ist.
     */
    public boolean isFieldValidationEnabled(String fieldName) {
        if (!enabled) {
            return false;
        }
        return delivery.getOrDefault(fieldName, true);
    }

    /**
     * Prüft ob die globale Validierung aktiviert ist.
     */
    public boolean isValidationEnabled() {
        return enabled;
    }
}

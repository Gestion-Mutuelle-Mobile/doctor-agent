package org.medecine.models;

/**
 * Représente l'état d'une consultation médicale
 */
public enum ConsultationState {
    WAITING_FOR_PATIENT("En attente d'un patient"),
    CONSULTATION_IN_PROGRESS("Consultation en cours"),
    DIAGNOSTIC_COMPLETE("Diagnostic terminé"),
    ERROR("Erreur");

    private final String description;

    ConsultationState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
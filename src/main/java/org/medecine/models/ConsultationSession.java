package org.medecine.models;

/**
 * Contient les informations de session d'une consultation
 */
public class ConsultationSession {
    private String userId;
    private String sessionId;
    private String patientName;
    private String patientAgentId;
    private ConsultationState state;
    private int messageCount;

    public ConsultationSession() {
        this.state = ConsultationState.WAITING_FOR_PATIENT;
        this.messageCount = 0;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientAgentId() {
        return patientAgentId;
    }

    public void setPatientAgentId(String patientAgentId) {
        this.patientAgentId = patientAgentId;
    }

    public ConsultationState getState() {
        return state;
    }

    public void setState(ConsultationState state) {
        this.state = state;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void incrementMessageCount() {
        this.messageCount++;
    }

    public void resetSession() {
        this.userId = null;
        this.sessionId = null;
        this.patientAgentId = null;
        this.patientName = null;
        this.state = ConsultationState.WAITING_FOR_PATIENT;
        this.messageCount = 0;
    }
}
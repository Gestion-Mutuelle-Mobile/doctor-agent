package org.medecine.models;

import com.google.gson.annotations.SerializedName;

/**
 * Représente une réponse du système expert
 */
public class ResponseMessage {
    private String status;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("session_id")
    private String sessionId;
    private String response;
    private String error;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isDiagnosticComplete() {
        return response != null && response.contains("END_DIAG");
    }

    public String getCleanResponse() {
        if (response == null) {
            return "";
        }
        return response.replace("END_DIAG", "").trim();
    }
}
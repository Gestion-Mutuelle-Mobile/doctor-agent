package org.medecine.utils;

import com.google.gson.Gson;
import org.medecine.models.ResponseMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Client pour interagir avec l'API du système expert
 */
public class APIClient {
    private final String baseUrl;
    private final Gson gson = new Gson();

    public APIClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    /**
     * Crée une nouvelle session dans le système expert
     */
    public ResponseMessage createSession() throws IOException {
        Map<String, Object> data = new HashMap<>();

        ResponseMessage response = sendRequest("api/session/new", "POST", data);

        // Ajouter du débogage
        System.out.println("Create Session Response: " + gson.toJson(response));

        if (response.getSessionId() == null || response.getUserId() == null) {
            System.err.println("WARNING: API returned null session ID or user ID!");
        }

        return response;
    }

    /**
     * Envoie un message au système expert
     */
    public ResponseMessage sendMessage(String userId, String sessionId, String message) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("session_id", sessionId);
        data.put("message", message);

        return sendRequest("api/chat", "POST", data);
    }

    /**
     * Termine une session dans le système expert
     */
    public ResponseMessage endSession(String userId, String sessionId) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("session_id", sessionId);

        return sendRequest("api/session/end", "POST", data);
    }

    /**
     * Vérifie la disponibilité du système expert
     */
    public boolean checkHealth() {
        try {
            ResponseMessage response = sendRequest("api/health", "GET", null);
            return "ok".equals(response.getStatus());
        } catch (IOException e) {
            return false;
        }
    }

    private ResponseMessage sendRequest(String endpoint, String method, Map<String, Object> data) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        System.out.println("Sending " + method + " request to: " + url);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(50000);
        connection.setReadTimeout(300000);

        if (method.equals("POST") || method.equals("PUT")) {
            connection.setDoOutput(true);
            String jsonInput = data != null ? gson.toJson(data) : "{}";
            System.out.println("Request body: " + jsonInput);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Response code: " + responseCode);

        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBody.append(line);
            }
        }

        String responseStr = responseBody.toString();
        System.out.println("Response body: " + responseStr);

        if (responseCode >= 400) {
            ResponseMessage errorResponse = new ResponseMessage();
            errorResponse.setStatus("error");
            errorResponse.setError("API Error: " + responseCode + " - " + responseStr);
            return errorResponse;
        }

        try {
            ResponseMessage responseObj = gson.fromJson(responseStr, ResponseMessage.class);
            if (responseObj == null) {
                ResponseMessage fallback = new ResponseMessage();
                fallback.setStatus("error");
                fallback.setError("Failed to parse response as ResponseMessage: null object returned");
                return fallback;
            }
            return responseObj;
        } catch (Exception e) {
            System.err.println("Exception parsing JSON response: " + e.getMessage());
            System.err.println("JSON was: " + responseStr);

            ResponseMessage errorResponse = new ResponseMessage();
            errorResponse.setStatus("error");
            errorResponse.setError("Failed to parse API response: " + e.getMessage());
            return errorResponse;
        }
    }

}
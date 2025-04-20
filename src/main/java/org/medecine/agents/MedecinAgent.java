package org.medecine.agents;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import org.medecine.behaviours.medecin.WaitForPatientBehaviour;
import org.medecine.gui.ConsoleManager;
import org.medecine.models.ConsultationSession;
import org.medecine.utils.APIClient;
import org.medecine.utils.AgentFinder;

public class MedecinAgent extends Agent {
    private APIClient apiClient;
    private ConsultationSession currentSession;

    @Override
    protected void setup() {
        // Initialiser les variables
        Object[] args = getArguments();
        String apiUrl = "http://localhost:8000";

        if (args != null && args.length > 0 && args[0] != null) {
            apiUrl = args[0].toString();
        }

        apiClient = new APIClient(apiUrl);
        currentSession = new ConsultationSession();

        // Vérifier la connexion à l'API
        ConsoleManager.clearScreen();
        ConsoleManager.printAsciiArt("ascii-art-medecin.txt");
        ConsoleManager.printHeader("AGENT MÉDECIN - " + getLocalName());
        ConsoleManager.printInfo("Initialisation de l'agent Médecin...");
        ConsoleManager.printInfo("API configurée: " + apiUrl);

        boolean apiAvailable = apiClient.checkHealth();
        if (!apiAvailable) {
            ConsoleManager.printError("Impossible de se connecter au système expert. Vérifiez l'URL et réessayez.");
            doDelete();
            return;
        }

        ConsoleManager.printSuccess("Connexion établie avec le système expert!");

        // S'enregistrer dans le service DF
        AgentFinder.registerService(this, "medecin-service");

        // Ajouter le comportement d'attente de patient
        SequentialBehaviour mainBehaviour = new SequentialBehaviour(this);
        mainBehaviour.addSubBehaviour(new WaitForPatientBehaviour(this));
        addBehaviour(mainBehaviour);

        ConsoleManager.printSuccess("Agent Médecin démarré et prêt à recevoir des patients!");
    }

    @Override
    protected void takeDown() {
        // Se désenregistrer des services
        AgentFinder.deregisterAgent(this);

        // Nettoyer la session si nécessaire
        if (currentSession != null && currentSession.getSessionId() != null) {
            try {
                apiClient.endSession(currentSession.getUserId(), currentSession.getSessionId());
            } catch (Exception e) {
                // Ignorer silencieusement les erreurs lors de la fermeture
            }
        }

        ConsoleManager.printInfo("Agent Médecin " + getLocalName() + " terminé.");
    }

    public APIClient getApiClient() {
        return apiClient;
    }

    public ConsultationSession getCurrentSession() {
        return currentSession;  // Doit être un champ d'instance, pas une variable locale
    }
}
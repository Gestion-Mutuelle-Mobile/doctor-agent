package org.medecine.behaviours.medecin;

import jade.core.behaviours.OneShotBehaviour;
import org.medecine.agents.MedecinAgent;
import org.medecine.gui.ConsoleManager;
import org.medecine.models.ConsultationSession;
import org.medecine.models.ResponseMessage;

import java.io.IOException;

/**
 * Comportement pour demander explicitement un diagnostic à l'API
 */
public class DiagnosticRequestBehaviour extends OneShotBehaviour {
    private final MedecinAgent agent;

    public DiagnosticRequestBehaviour(MedecinAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        ConsultationSession session = agent.getCurrentSession();

        // Vérifier si nous sommes en consultation
        if (session.getSessionId() == null) {
            ConsoleManager.printWarning("Aucune session active, impossible de demander un diagnostic");
            return;
        }

        ConsoleManager.printInfo("Demande d'un diagnostic complet au système expert...");

        try {
            // Envoyer une demande spécifique de diagnostic
            ResponseMessage apiResponse = agent.getApiClient().sendMessage(
                    session.getUserId(),
                    session.getSessionId(),
                    "Pouvez-vous me fournir un diagnostic complet basé sur tous les symptômes mentionnés jusqu'à présent?"
            );

            if ("success".equals(apiResponse.getStatus())) {
                // Afficher le diagnostic
                String diagnosticText = apiResponse.getCleanResponse();
                ConsoleManager.printSuccess("Diagnostic reçu:");
                ConsoleManager.printInfo(diagnosticText);
            } else {
                // Erreur de l'API
                ConsoleManager.printError("Erreur lors de la demande de diagnostic: " + apiResponse.getError());
            }
        } catch (IOException e) {
            ConsoleManager.printError("Exception lors de la communication avec le système expert: " + e.getMessage());
        }
    }
}
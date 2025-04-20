package org.medecine.behaviours.patient;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.medecine.gui.ConsoleManager;

/**
 * Comportement pour écouter activement les réponses du médecin
 * Utilise TickerBehaviour pour vérifier périodiquement les nouveaux messages
 */
public class DoctorResponseBehaviour extends TickerBehaviour {
    private final Agent agent;
    private final AID medecinAID;

    public DoctorResponseBehaviour(Agent agent, AID medecinAID) {
        // Vérifier toutes les 100ms
        super(agent, 100);
        this.agent = agent;
        this.medecinAID = medecinAID;
    }

    @Override
    protected void onTick() {
        // Template pour ne recevoir que les messages du médecin
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
                ),
                MessageTemplate.MatchSender(medecinAID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            String content = message.getContent();

            // Debug
            System.out.println("DEBUG: Patient a reçu un message du médecin: " + content);

            // Vérifier si c'est un message de fin de consultation
            if ("CONSULTATION_ENDED".equals(content)) {
                ConsoleManager.printInfo("Le médecin a terminé la consultation.");
                ConsoleManager.printSystem("Vous pouvez fermer cette fenêtre ou taper 'exit' pour quitter.");
            } else if (message.getPerformative() == ACLMessage.FAILURE) {
                // Erreur du médecin
                ConsoleManager.printWarning("Message d'erreur du médecin: " + content);
            } else {
                // Message normal du médecin
                ConsoleManager.printDoctorMessage("Dr. " + medecinAID.getLocalName(), content);
            }
        }
        // Ne pas bloquer, nous sommes dans un TickerBehaviour
    }
}
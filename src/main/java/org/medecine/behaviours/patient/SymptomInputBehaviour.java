package org.medecine.behaviours.patient;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import org.medecine.gui.ConsoleManager;
import org.medecine.utils.ConsoleUtils;

import java.util.Scanner;

/**
 * Comportement permettant au patient de saisir ses symptômes
 * Version modifiée qui n'attend pas les réponses (gérées par DoctorResponseBehaviour)
 */
public class SymptomInputBehaviour extends OneShotBehaviour {
    private final Agent agent;
    private final ConsultationBehaviour consultationBehaviour;
    private final AID medecinAID;
    private final Scanner scanner = new Scanner(System.in);

    public SymptomInputBehaviour(Agent agent, ConsultationBehaviour consultationBehaviour, AID medecinAID) {
        this.agent = agent;
        this.consultationBehaviour = consultationBehaviour;
        this.medecinAID = medecinAID;
    }

    @Override
    public void onStart() {
        ConsoleManager.printSystem("La consultation commence maintenant. Décrivez vos symptômes ou posez des questions au médecin.");
        ConsoleManager.printSystem("Tapez 'exit' pour terminer la consultation.");
    }

    @Override
    public void action() {
        // Créer une boucle infinie pour la saisie des symptômes
        while (consultationBehaviour.isActive()) {
            // Saisir un symptôme ou une question
            String input = ConsoleUtils.readLine("> ");

            if (input.trim().isEmpty()) {
                continue;
            }

            // Commande de sortie
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                consultationBehaviour.endConsultation();
                break;
            }

            // Envoyer le message au médecin
            jade.lang.acl.ACLMessage symptomMessage = new jade.lang.acl.ACLMessage(jade.lang.acl.ACLMessage.INFORM);
            symptomMessage.addReceiver(medecinAID);
            symptomMessage.setContent(input);

            // Debug
            System.out.println("DEBUG Patient: Envoi message au médecin");
            System.out.println("DEBUG Patient: Mon AID = " + agent.getAID().getName());
            System.out.println("DEBUG Patient: AID médecin = " + medecinAID.getName());

            agent.send(symptomMessage);

            ConsoleManager.printPatientMessage(agent.getLocalName(), input);
        }
    }
}
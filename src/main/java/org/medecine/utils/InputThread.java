package org.medecine.utils;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.medecine.behaviours.patient.ConsultationBehaviour;
import org.medecine.gui.ConsoleManager;

/**
 * Thread pour gérer les entrées utilisateur pendant la consultation
 */
public class InputThread extends Thread {
    private final Agent agent;
    private final ConsultationBehaviour consultationBehaviour;
    private final AID medecinAID;
    private volatile boolean running = true;

    public InputThread(Agent agent, ConsultationBehaviour consultationBehaviour, AID medecinAID) {
        this.agent = agent;
        this.consultationBehaviour = consultationBehaviour;
        this.medecinAID = medecinAID;
    }

    @Override
    public void run() {
        ConsoleManager.printSystem("La consultation commence maintenant. Décrivez vos symptômes ou posez des questions au médecin.");
        ConsoleManager.printSystem("Tapez 'exit' pour terminer la consultation.");

        java.util.Scanner scanner = new java.util.Scanner(System.in);

        while (running && consultationBehaviour.isActive()) {
            try {
                // Afficher le prompt
                System.out.print("> ");
                // Lire l'entrée utilisateur
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                // Commande de sortie
                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                    consultationBehaviour.endConsultation();
                    running = false;
                    break;
                }

                // Envoyer le message au médecin
                ACLMessage symptomMessage = new ACLMessage(ACLMessage.INFORM);
                symptomMessage.addReceiver(medecinAID);
                symptomMessage.setContent(input);

                // Debug
                System.out.println("DEBUG Patient: Envoi message au médecin");
                System.out.println("DEBUG Patient: Mon AID = " + agent.getAID().getName());
                System.out.println("DEBUG Patient: AID médecin = " + medecinAID.getName());

                agent.send(symptomMessage);

                ConsoleManager.printPatientMessage(agent.getLocalName(), input);
            } catch (Exception e) {
                System.err.println("Erreur lors de la lecture de l'entrée: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Thread d'entrée terminé.");
    }

    public void stopInput() {
        running = false;
        // Interrompre le thread si nécessaire
        interrupt();
    }
}
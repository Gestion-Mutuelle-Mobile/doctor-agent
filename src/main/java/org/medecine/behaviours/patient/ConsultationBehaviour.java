package org.medecine.behaviours.patient;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.medecine.gui.ConsoleManager;
import org.medecine.utils.InputThread;

public class ConsultationBehaviour extends CyclicBehaviour {
    private final Agent agent;
    private final AID medecinAID;
    private enum ConsultationState { REQUESTING, WAITING, ACTIVE, ENDED }
    private ConsultationState state = ConsultationState.REQUESTING;
    private InputThread inputThread = null;

    public ConsultationBehaviour(Agent agent, AID medecinAID) {
        this.agent = agent;
        this.medecinAID = medecinAID;
    }

    @Override
    public void onStart() {
        // Envoyer une demande de consultation au médecin
        sendConsultationRequest();
    }

    @Override
    public void action() {
        switch (state) {
            case REQUESTING:
                // Attendre la réponse à notre demande de consultation
                MessageTemplate template = MessageTemplate.and(
                        MessageTemplate.or(
                                MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                                MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                        ),
                        MessageTemplate.MatchSender(medecinAID)
                );

                ACLMessage response = agent.receive(template);
                if (response != null) {
                    if (response.getPerformative() == ACLMessage.AGREE) {
                        // Consultation acceptée
                        ConsoleManager.printSuccess("Consultation acceptée par Dr. " + medecinAID.getLocalName());
                        state = ConsultationState.ACTIVE;

                        // Démarrer le thread d'entrée utilisateur
                        inputThread = new InputThread(agent, this, medecinAID);
                        inputThread.start();

                        // Ajouter le comportement d'écoute des réponses
                        agent.addBehaviour(new DoctorResponseBehaviour(agent, medecinAID));
                    } else {
                        // Consultation refusée
                        String reason = response.getContent().contains(":") ?
                                response.getContent().split(":", 2)[1] : "Raison inconnue";
                        ConsoleManager.printError("Consultation refusée: " + reason);
                        state = ConsultationState.ENDED;
                        agent.doDelete();
                    }
                } else {
                    block();
                }
                break;

            case ACTIVE:
                // Rien à faire, tout est géré par le InputThread et DoctorResponseBehaviour
                block();
                break;

            case WAITING:
                // Attendre un message de fin de consultation
                MessageTemplate endTemplate = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.and(
                                MessageTemplate.MatchSender(medecinAID),
                                MessageTemplate.MatchContent("CONSULTATION_ENDED")
                        )
                );

                ACLMessage endMessage = agent.receive(endTemplate);
                if (endMessage != null) {
                    state = ConsultationState.ENDED;

                    // Arrêter le thread d'entrée
                    if (inputThread != null) {
                        inputThread.stopInput();
                    }

                    ConsoleManager.printInfo("Consultation terminée.");

                    // Demander à l'utilisateur s'il veut quitter
                    System.out.print("Voulez-vous quitter l'application? [O/n]: ");
                    String response2 = new java.util.Scanner(System.in).nextLine().trim();
                    if (response2.isEmpty() || response2.toLowerCase().startsWith("o") || response2.toLowerCase().startsWith("y")) {
                        agent.doDelete();
                    }
                } else {
                    block();
                }
                break;

            case ENDED:
                // Rien à faire
                block();
                break;
        }
    }

    private void sendConsultationRequest() {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(medecinAID);
        request.setContent("CONSULTATION_REQUEST");
        agent.send(request);

        ConsoleManager.printInfo("Demande de consultation envoyée à Dr. " + medecinAID.getLocalName());
        ConsoleManager.printLoadingDots("En attente de réponse", 5, 500);
        state = ConsultationState.REQUESTING;
    }

    public void endConsultation() {
        if (state == ConsultationState.ACTIVE) {
            // Envoyer un message de fin au médecin
            ACLMessage endMessage = new ACLMessage(ACLMessage.INFORM);
            endMessage.addReceiver(medecinAID);
            endMessage.setContent("END_CONSULTATION");
            agent.send(endMessage);

            state = ConsultationState.WAITING;
            ConsoleManager.printInfo("Demande de fin de consultation envoyée.");
        }
    }

    public boolean isActive() {
        return state == ConsultationState.ACTIVE;
    }

    @Override
    public int onEnd() {
        // Arrêter le thread d'entrée si ce n'est pas déjà fait
        if (inputThread != null) {
            inputThread.stopInput();
        }
        return super.onEnd();
    }
}
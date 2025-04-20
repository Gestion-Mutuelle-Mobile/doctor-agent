package org.medecine.behaviours.medecin;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.medecine.agents.MedecinAgent;
import org.medecine.behaviours.medecin.ConsultationBehaviour;
import org.medecine.gui.ConsoleManager;
import org.medecine.models.ConsultationSession;
import org.medecine.models.ConsultationState;
import org.medecine.models.ResponseMessage;

public class WaitForPatientBehaviour extends CyclicBehaviour {
    private final MedecinAgent agent;

    public WaitForPatientBehaviour(MedecinAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        // Ne traiter les messages que si le médecin est en attente d'un patient
        ConsultationSession session = agent.getCurrentSession();
        if (session.getState() != ConsultationState.WAITING_FOR_PATIENT) {
            block();
            return;
        }

        // Attendre un message de type REQUEST
        MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage message = agent.receive(template);

        if (message != null) {
            String patientName = message.getSender().getLocalName();
            String content = message.getContent();

            if (content != null && content.startsWith("CONSULTATION_REQUEST")) {
                // Un patient demande une consultation
                ConsoleManager.printInfo("Nouvelle demande de consultation de " + patientName);

                try {
                    // Créer une nouvelle session avec l'API
                    ResponseMessage apiResponse = agent.getApiClient().createSession();

                    if ("success".equals(apiResponse.getStatus())) {
                        // Configurer la session
                        // Configurer la session
                        session.setUserId(apiResponse.getUserId());
                        session.setSessionId(apiResponse.getSessionId());
                        session.setPatientName(patientName);
                        // Utilisez uniquement le localName pour simplifier
                        session.setPatientAgentId(message.getSender().getLocalName());
                        System.out.println("DEBUG: ID patient stocké: " + session.getPatientAgentId());
                        session.setState(ConsultationState.CONSULTATION_IN_PROGRESS);

                        // Envoyer une confirmation au patient
                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setContent("CONSULTATION_ACCEPTED");
                        agent.send(reply);

                        ConsoleManager.printSuccess("Consultation acceptée avec " + patientName);
                        ConsoleManager.printInfo("Session ID: " + session.getSessionId());

                        // Ajouter le comportement de consultation
                        agent.addBehaviour(new ConsultationBehaviour(agent));
                    } else {
                        // Erreur lors de la création de la session
                        ConsoleManager.printError("Erreur lors de la création de la session: " + apiResponse.getError());

                        // Envoyer un refus au patient
                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("CONSULTATION_REFUSED:Erreur système");
                        agent.send(reply);
                    }
                } catch (Exception e) {
                    ConsoleManager.printError("Exception lors du traitement de la demande de consultation: " + e.getMessage());

                    // Envoyer un refus au patient
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("CONSULTATION_REFUSED:Exception système");
                    agent.send(reply);
                }
            }
        } else {
            block();
        }
    }
}
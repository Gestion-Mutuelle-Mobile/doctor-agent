package org.medecine.behaviours.medecin;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.medecine.agents.MedecinAgent;
import org.medecine.gui.ConsoleManager;
import org.medecine.models.ConsultationSession;
import org.medecine.models.ConsultationState;
import org.medecine.models.ResponseMessage;

import java.io.IOException;

public class ConsultationBehaviour extends CyclicBehaviour {
    private final MedecinAgent agent;

    public ConsultationBehaviour(MedecinAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        ConsultationSession session = agent.getCurrentSession();
        System.out.println("DEBUG: État actuel de session.getPatientAgentId() = " + session.getPatientAgentId());

        // Ne traiter que si en mode consultation
        if (session.getState() != ConsultationState.CONSULTATION_IN_PROGRESS) {
            System.out.println("Doctor: Not in consultation state, state is: " + session.getState());
            // Si le diagnostic est terminé, réinitialiser la session et supprimer ce comportement
            if (session.getState() == ConsultationState.DIAGNOSTIC_COMPLETE) {
                session.resetSession();
                session.setState(ConsultationState.WAITING_FOR_PATIENT);
                agent.removeBehaviour(this);
            }
            block();
            return;
        }

        System.out.println("Doctor: Listening for messages from patient: " + session.getPatientAgentId());

        // Attendre un message du patient en cours de consultation
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchSender(new jade.core.AID(session.getPatientAgentId(), AID.ISLOCALNAME))
        );

        // Débogage - récupérer tous les messages
        ACLMessage anyMessage = agent.receive();
        if (anyMessage != null) {
            System.out.println("DEBUG: Message reçu de: " + anyMessage.getSender().getName());
            System.out.println("DEBUG: Contenu: " + anyMessage.getContent());
            System.out.println("DEBUG: Performatif: " + ACLMessage.getPerformative(anyMessage.getPerformative()));
            agent.putBack(anyMessage);
        } else {
            System.out.println("DEBUG: Aucun message en file d'attente");
        }

        ACLMessage message = agent.receive(template);

        if (message != null) {
            System.out.println("Doctor: Received message from patient: " + message.getContent());

            String content = message.getContent();

            // Si le patient veut terminer la consultation
            if ("END_CONSULTATION".equals(content)) {
                handleEndConsultation(session);
                return;
            }

            // Sinon, traiter le message comme un symptôme ou une question
            ConsoleManager.printPatientMessage(session.getPatientName(), content);

            // Incrémenter le compteur de messages
            session.incrementMessageCount();

            // Simuler un peu de "réflexion" du médecin
            ConsoleManager.printLoadingDots("Analyse en cours", 3, 700);

            try {
                // Envoyer le message au système expert
                ResponseMessage apiResponse = agent.getApiClient().sendMessage(
                        session.getUserId(),
                        session.getSessionId(),
                        content
                );

                if ("success".equals(apiResponse.getStatus())) {
                    // Récupérer la réponse
                    String responseText = apiResponse.getCleanResponse();

                    // Afficher la réponse
                    ConsoleManager.printDoctorMessage(agent.getLocalName(), responseText);

                    // MODIFICATION ICI: Vérifier si la réponse commence par "DIAGNOSTIC:"
                    if (responseText.startsWith("DIAGNOSTIC:")) {
                        // C'est un diagnostic - utiliser PROPOSE au lieu de INFORM
                        ACLMessage diagReply = message.createReply();
                        diagReply.setPerformative(ACLMessage.PROPOSE); // Utiliser PROPOSE pour le diagnostic
                        diagReply.setContent(responseText); // Garder le préfixe "DIAGNOSTIC:"
                        agent.send(diagReply);

                        ConsoleManager.printSuccess("Diagnostic envoyé au patient: " + session.getPatientName());

                        // Attendre un peu avant de traiter la fin du diagnostic
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt();
//                        }
//
//                        // Terminer la consultation après envoi du diagnostic
//                        ConsoleManager.printInfo("Diagnostic terminé avec " + session.getPatientName());
//                        handleEndConsultation(session);
                    } else {
                        // Réponse normale - utiliser INFORM
                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(responseText);
                        agent.send(reply);
                    }

                    // L'ancienne vérification pour END_DIAG (remplacée par la détection de DIAGNOSTIC: ci-dessus)
                    if (apiResponse.isDiagnosticComplete()) {
                        // Attendre un peu avant de traiter la fin du diagnostic
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        ConsoleManager.printInfo("Diagnostic terminé avec " + session.getPatientName());
                        handleEndConsultation(session);
                    }
                } else {
                    // Erreur de l'API
                    ConsoleManager.printError("Erreur du système expert: " + apiResponse.getError());

                    // Envoyer un message d'erreur au patient
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Désolé, j'ai rencontré un problème technique. Pouvez-vous reformuler?");
                    agent.send(reply);
                }
            } catch (IOException e) {
                ConsoleManager.printError("Exception lors de la communication avec le système expert: " + e.getMessage());

                // Envoyer un message d'erreur au patient
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("Désolé, j'ai des difficultés à accéder au système expert. Pouvez-vous patienter un instant?");
                agent.send(reply);
            }
        } else {
            block();
        }
    }

    private void handleEndConsultation(ConsultationSession session) {
        try {
            // Terminer la session dans l'API
            agent.getApiClient().endSession(session.getUserId(), session.getSessionId());

            // Envoyer un message de fin au patient
            ACLMessage endMessage = new ACLMessage(ACLMessage.INFORM);
            endMessage.addReceiver(new jade.core.AID(session.getPatientAgentId(), false));
            endMessage.setContent("CONSULTATION_ENDED");
            agent.send(endMessage);

            // Mettre à jour l'état de la session
            session.setState(ConsultationState.DIAGNOSTIC_COMPLETE);

            ConsoleManager.printSuccess("Consultation avec " + session.getPatientName() + " terminée avec succès.");
            ConsoleManager.printInfo("En attente d'un nouveau patient...");
        } catch (IOException e) {
            ConsoleManager.printError("Erreur lors de la fermeture de la session: " + e.getMessage());
        }
    }
}
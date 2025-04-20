package org.medecine.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import org.medecine.behaviours.patient.ConsultationBehaviour;
import org.medecine.behaviours.patient.DoctorResponseBehaviour;
import org.medecine.gui.ConsoleManager;
import org.medecine.utils.AgentFinder;

public class PatientAgent extends Agent {
    private AID medecinAID;
    private String targetMedecinName;

    @Override
    protected void setup() {
        // Initialiser les variables
        Object[] args = getArguments();
        targetMedecinName = "medecin";  // Médecin par défaut

        if (args != null && args.length > 0 && args[0] != null) {
            targetMedecinName = args[0].toString();
        }

        // Affichage de l'interface
        ConsoleManager.clearScreen();
        ConsoleManager.printAsciiArt("ascii-art-patient.txt");
        ConsoleManager.printHeader("AGENT PATIENT - " + getLocalName());
        ConsoleManager.printInfo("Initialisation de l'agent Patient...");

        // S'enregistrer dans le service DF
        AgentFinder.registerService(this, "patient-service");

        // Chercher le médecin après un court délai
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                findMedecin();
            }
        });
    }

    private void findMedecin() {
        // Recherche directe par nom
        medecinAID = new AID(targetMedecinName, AID.ISLOCALNAME);

        // Si la recherche directe ne fonctionne pas, chercher via le service DF
        ConsoleManager.printInfo("Recherche du médecin '" + targetMedecinName + "'...");
        ConsoleManager.printLoadingDots("En attente du médecin", 5, 500);

        try {
            // Attendre jusqu'à 10 secondes que le médecin soit disponible
            int attempts = 0;
            while (attempts < 10) {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("medecin-service");
                template.addServices(sd);

                DFAgentDescription[] results = DFService.search(this, template);
                if (results.length > 0) {
                    for (DFAgentDescription result : results) {
                        if (result.getName().getLocalName().equals(targetMedecinName)) {
                            medecinAID = result.getName();
                            startConsultation();
                            return;
                        }
                    }

                    // Si le médecin spécifique n'est pas trouvé, prendre le premier disponible
                    if (results.length > 0) {
                        medecinAID = results[0].getName();
                        ConsoleManager.printWarning("Médecin '" + targetMedecinName + "' non trouvé. Connexion à '" + medecinAID.getLocalName() + "' à la place.");
                        startConsultation();
                        return;
                    }
                }

                attempts++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Si aucun médecin n'est trouvé après les tentatives
            ConsoleManager.printError("Aucun médecin disponible. Veuillez réessayer plus tard.");
            doDelete();

        } catch (FIPAException e) {
            ConsoleManager.printError("Erreur lors de la recherche du médecin: " + e.getMessage());
            doDelete();
        }
    }

    private void startConsultation() {
        ConsoleManager.printSuccess("Médecin trouvé: " + medecinAID.getLocalName());
        ConsoleManager.printInfo("Début de la consultation...");

        // Ajouter uniquement le comportement de consultation
        // Il gérera lui-même l'ajout de DoctorResponseBehaviour et le démarrage du thread d'entrée
        addBehaviour(new ConsultationBehaviour(this, medecinAID));
    }

    @Override
    protected void takeDown() {
        // Se désenregistrer des services
        AgentFinder.deregisterAgent(this);

        ConsoleManager.printInfo("Agent Patient " + getLocalName() + " terminé.");
    }
}
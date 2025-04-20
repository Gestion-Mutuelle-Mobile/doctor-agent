package org.medecine.jade;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import java.util.Scanner;

public class PatientContainer {
    public static void main(String[] args) {
        try {
            // Affichage du titre
            System.out.println("╔═══════════════════════════════════════════════════╗");
            System.out.println("║     SYSTÈME DE CONSULTATION MÉDICALE - PATIENT    ║");
            System.out.println("╚═══════════════════════════════════════════════════╝");

            // Obtenir l'instance du runtime JADE
            Runtime runtime = Runtime.instance();

            // Créer un profil pour le conteneur de l'agent patient
            ProfileImpl profile = new ProfileImpl(false);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Adresse IP du conteneur principal (laisser vide pour localhost): ");
            String mainHost = scanner.nextLine().trim();

            if (mainHost.isEmpty()) {
                mainHost = "localhost";
            }

            profile.setParameter(Profile.MAIN_HOST, mainHost);
            profile.setParameter(Profile.MAIN_PORT, "1099"); // Port par défaut de JADE

            // Créer le conteneur
            AgentContainer patientContainer = runtime.createAgentContainer(profile);
            System.out.println("Conteneur PATIENT démarré avec succès!");

            // Demander le nom du patient
            System.out.print("Votre nom: ");
            String patientName = scanner.nextLine().trim();
            if (patientName.isEmpty()) {
                patientName = "patient-" + System.currentTimeMillis() % 1000;
            }

            System.out.print("Nom de l'agent médecin à contacter [medecin]: ");
            String medecinName = scanner.nextLine().trim();
            if (medecinName.isEmpty()) {
                medecinName = "medecin";
            }

            Object[] patientArgs = new Object[]{medecinName};
            AgentController patientController = patientContainer.createNewAgent(
                    patientName, "org.medecine.agents.PatientAgent", patientArgs);

            // Démarrer l'agent
            patientController.start();
            System.out.println("Agent Patient '" + patientName + "' démarré avec succès!");

        } catch (ControllerException e) {
            System.err.println("❌ Erreur lors du démarrage du conteneur PATIENT:");
            e.printStackTrace();
        }
    }
}
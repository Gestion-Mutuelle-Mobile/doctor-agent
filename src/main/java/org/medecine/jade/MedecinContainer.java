package org.medecine.jade;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import java.util.Scanner;

public class MedecinContainer {
    public static void main(String[] args) {
        try {
            // Affichage du titre
            System.out.println("╔═══════════════════════════════════════════════════╗");
            System.out.println("║     SYSTÈME DE CONSULTATION MÉDICALE - MÉDECIN    ║");
            System.out.println("╚═══════════════════════════════════════════════════╝");

            // Obtenir l'instance du runtime JADE
            Runtime runtime = Runtime.instance();

            // Créer un profil pour le conteneur de l'agent médecin
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
            AgentContainer medecinContainer = runtime.createAgentContainer(profile);
            System.out.println("Conteneur MÉDECIN démarré avec succès!");

            // Créer l'agent médecin
            String medecinName = "medecin";
            System.out.print("Nom de l'agent médecin [" + medecinName + "]: ");
            String customName = scanner.nextLine().trim();
            if (!customName.isEmpty()) {
                medecinName = customName;
            }

            System.out.print("URL de l'API du système expert [http://localhost:8000]: ");
            String apiUrl = scanner.nextLine().trim();
            if (apiUrl.isEmpty()) {
                apiUrl = "http://localhost:8000";
            }

            Object[] medecinArgs = new Object[]{apiUrl};
            AgentController medecinController = medecinContainer.createNewAgent(
                    medecinName, "org.medecine.agents.MedecinAgent", medecinArgs);

            // Démarrer l'agent
            medecinController.start();
            System.out.println("Agent Médecin '" + medecinName + "' démarré avec succès!");

        } catch (ControllerException e) {
            System.err.println("❌ Erreur lors du démarrage du conteneur MÉDECIN:");
            e.printStackTrace();
        }
    }
}
package org.medecine.jade;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;

public class MainContainer {
    private static AgentContainer mainContainer;

    public static void main(String[] args) {
        try {
            // Obtenir l'instance du runtime JADE
            Runtime runtime = Runtime.instance();

            // Créer un profil avec GUI et en tant que conteneur principal
            ProfileImpl profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true");
            profile.setParameter(Profile.MAIN, "true");

            // Créer le conteneur principal
            mainContainer = runtime.createMainContainer(profile);
            System.out.println("Conteneur principal démarré avec succès!");

            // Maintenir le conteneur actif
            mainContainer.start();

        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour accéder au conteneur principal depuis d'autres classes (non nécessaire maintenant)
    public static AgentContainer getMainContainer() {
        return mainContainer;
    }
}
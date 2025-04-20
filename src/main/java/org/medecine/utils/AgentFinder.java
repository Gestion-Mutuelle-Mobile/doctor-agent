package org.medecine.utils;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

/**
 * Utilitaire pour rechercher des agents dans la plateforme JADE
 */
public class AgentFinder {

    /**
     * Recherche un agent par son nom
     */
    public static AID findAgentByName(String name) {
        return new AID(name, AID.ISLOCALNAME);
    }

    /**
     * Enregistre un agent dans le service DF (Directory Facilitator)
     */
    public static void registerService(Agent agent, String type) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(agent.getLocalName() + "-" + type);

        dfd.addServices(sd);

        try {
            // Vérifier si l'agent est déjà enregistré avant de désenregistrer
            try {
                DFAgentDescription[] result = DFService.search(agent, dfd);
                if (result != null && result.length > 0) {
                    // L'agent est déjà enregistré, donc on le désenregistre
                    DFService.deregister(agent);
                }
            } catch (Exception e) {
                // Ignorer l'exception si la recherche échoue
            }

            // Puis enregistrer
            DFService.register(agent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recherche des agents par type de service
     */
    public static AID[] findAgentsByType(Agent agent, String type) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        template.addServices(sd);

        try {
            DFAgentDescription[] results = DFService.search(agent, template);
            AID[] agents = new AID[results.length];

            for (int i = 0; i < results.length; i++) {
                agents[i] = results[i].getName();
            }

            return agents;
        } catch (FIPAException e) {
            e.printStackTrace();
            return new AID[0];
        }
    }

    /**
     * Désenregistre un agent du service DF
     */
    public static void deregisterAgent(Agent agent) {
        try {
            DFService.deregister(agent);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
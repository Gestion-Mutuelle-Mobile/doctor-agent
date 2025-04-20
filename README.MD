# Système de Consultation Médicale avec Agents JADE

## Table des matières
1. [Introduction](#introduction)
2. [Architecture du système](#architecture-du-système)
3. [Prérequis](#prérequis)
4. [Installation](#installation)
5. [Configuration](#configuration)
6. [Utilisation](#utilisation)
    - [Démarrer le conteneur principal](#démarrer-le-conteneur-principal)
    - [Démarrer l'agent médecin](#démarrer-lagent-médecin)
    - [Démarrer l'agent patient](#démarrer-lagent-patient)
    - [Déroulement d'une consultation](#déroulement-dune-consultation)
7. [Structure du code](#structure-du-code)
8. [Dépannage](#dépannage)
9. [Extensions possibles](#extensions-possibles)
10. [Licence](#licence)

## Introduction

Ce système implémente une application de consultation médicale à distance basée sur l'architecture multi-agents JADE (Java Agent DEvelopment Framework). Le système permet à un agent Patient de communiquer avec un agent Médecin, qui utilise lui-même un système expert ADK via une API REST pour fournir des réponses médicales pertinentes.

Le système est conçu pour fonctionner en environnement distribué, les agents pouvant s'exécuter sur des machines différentes mais connectées au même réseau.

## Architecture du système

L'architecture du système repose sur trois composants principaux :

1. **Agent Patient** : Interface utilisateur permettant à un patient de communiquer ses symptômes et de recevoir des réponses médicales.
2. **Agent Médecin** : Interface de traitement des messages du patient, utilisant une API externe pour générer des réponses médicales appropriées.
3. **Système Expert ADK** : API REST Python qui implémente un système expert médical capable d'analyser les symptômes et de proposer des diagnostics.

La communication entre les agents est assurée par le framework JADE, et la communication avec le système expert est réalisée via HTTP/REST.

## Prérequis

- Java Development Kit (JDK) 11 ou supérieur
- Maven 3.6.0 ou supérieur
- Python 3.7 ou supérieur (pour le système expert ADK)
- Framework JADE 4.5.0
- Connexion réseau entre les machines si vous exécutez les agents sur différentes machines

## Installation

### 1. Cloner le dépôt

```bash
git clone https://github.com/yourusername/jade-medical-consultation.git
cd jade-medical-consultation
```

### 2. Installer les dépendances Maven

```bash
mvn clean install
```

### 3. Installer et démarrer le système expert ADK

```bash
# Dans un répertoire séparé
git clone https://github.com/yourusername/medical-expert-adk.git
cd medical-expert-adk
pip install -r requirements.txt
python api_server.py
```

## Configuration

### Configuration du Système Expert ADK

Par défaut, le système expert ADK s'exécute sur l'hôte local (localhost) sur le port 8000. Si vous souhaitez modifier cette configuration, vous pouvez éditer le fichier `api_server.py` du système expert.

### Configuration des Agents JADE

Les agents peuvent être configurés lors de leur démarrage. Vous pouvez spécifier :

- L'adresse du conteneur principal JADE
- Le nom des agents
- L'URL de l'API du système expert
- Le nom de l'agent médecin à contacter

## Utilisation

### Démarrer le conteneur principal

Le conteneur principal JADE doit être démarré en premier, car il héberge les services nécessaires à la communication entre les agents.

```bash
java -cp "lib/*:target/jade-medical-consultation-1.0.0.jar" org.medecine.jade.MainContainer
```

Un message confirmant le démarrage du conteneur principal s'affichera :

```
Conteneur principal démarré avec succès!
```

### Démarrer l'agent médecin

Une fois le conteneur principal démarré, lancez l'agent médecin :

```bash
java -cp "lib/*:target/jade-medical-consultation-1.0.0.jar" org.medecine.jade.MedecinContainer
```

Le programme vous demandera quelques informations :

```
╔═══════════════════════════════════════════════════╗
║     SYSTÈME DE CONSULTATION MÉDICALE - MÉDECIN    ║
╚═══════════════════════════════════════════════════╝
Adresse IP du conteneur principal (laisser vide pour localhost):
```

Entrez l'adresse IP du conteneur principal ou laissez vide pour utiliser localhost.

```
Nom de l'agent médecin [medecin]:
```

Vous pouvez spécifier un nom personnalisé ou laisser vide pour utiliser le nom par défaut "medecin".

```
URL de l'API du système expert [http://localhost:8000]:
```

Indiquez l'URL de l'API du système expert ou laissez vide pour utiliser l'URL par défaut.

Une fois configuré, l'agent médecin démarrera et sera prêt à recevoir des patients :

```
Agent Médecin 'medecin' démarré avec succès!
```

### Démarrer l'agent patient

Lancez l'agent patient, qui représente l'interface utilisateur pour le patient :

```bash
java -cp "lib/*:target/jade-medical-consultation-1.0.0.jar" org.medecine.jade.PatientContainer
```

Le programme vous demandera également quelques informations :

```
╔═══════════════════════════════════════════════════╗
║     SYSTÈME DE CONSULTATION MÉDICALE - PATIENT    ║
╚═══════════════════════════════════════════════════╝
Adresse IP du conteneur principal (laisser vide pour localhost):
```

Entrez l'adresse IP du conteneur principal ou laissez vide pour utiliser localhost.

```
Votre nom:
```

Entrez votre nom. Si vous laissez vide, un nom sera généré automatiquement.

```
Nom de l'agent médecin à contacter [medecin]:
```

Indiquez le nom de l'agent médecin à contacter ou laissez vide pour utiliser le nom par défaut "medecin".

Une fois configuré, l'agent patient recherchera l'agent médecin et démarrera la consultation :

```
Agent Patient 'votrenom' démarré avec succès!
Recherche du médecin 'medecin'...
En attente du médecin.....
Médecin trouvé: medecin
Début de la consultation...
```

### Déroulement d'une consultation

Une fois la connexion établie, le patient peut commencer à décrire ses symptômes :

```
La consultation commence maintenant. Décrivez vos symptômes ou posez des questions au médecin.
Tapez 'exit' pour terminer la consultation.
> J'ai mal à la tête et de la fièvre depuis hier.
```

Le médecin, via le système expert, traitera l'information et répondra :

```
Dr. medecin: Merci de me consulter. Pourriez-vous me préciser à quel endroit exactement vous ressentez cette douleur à la tête et à combien estimez-vous votre température ?
```

La consultation se poursuit avec l'échange de messages entre le patient et le médecin. Lorsque le système expert détermine qu'il dispose de suffisamment d'informations pour établir un diagnostic, il enverra un message spécial contenant "END_DIAG", ce qui mettra fin à la consultation.

Pour terminer manuellement la consultation, tapez simplement `exit` :

```
> exit
Demande de fin de consultation envoyée.
```

## Structure du code

La structure du projet est organisée de la manière suivante :

```
src/
├── main/
│   ├── java/
│   │   └── org/
│   │       └── medecine/
│   │           ├── agents/         # Classes des agents JADE
│   │           ├── behaviours/     # Comportements des agents
│   │           │   ├── medecin/    # Comportements spécifiques au médecin
│   │           │   └── patient/    # Comportements spécifiques au patient
│   │           ├── gui/            # Classes liées à l'interface console
│   │           ├── jade/           # Classes de conteneurs JADE
│   │           ├── models/         # Modèles de données
│   │           └── utils/          # Classes utilitaires
│   └── resources/                  # Ressources (art ASCII, configuration)
```

### Principaux composants

#### Agents
- **MedecinAgent**: Agent qui représente le médecin virtuel.
- **PatientAgent**: Agent qui représente l'interface patient.

#### Comportements
- **WaitForPatientBehaviour**: Attend la connexion d'un patient.
- **ConsultationBehaviour**: Gère la consultation médicale.
- **DoctorResponseBehaviour**: Écoute les réponses du médecin.
- **InputThread**: Thread séparé pour gérer les entrées utilisateur.

#### Modèles
- **ConsultationSession**: Contient les informations d'une session de consultation.
- **ConsultationState**: Enum représentant les états possibles d'une consultation.
- **ResponseMessage**: Représente une réponse du système expert.

#### Utilitaires
- **APIClient**: Client REST pour communiquer avec le système expert.
- **AgentFinder**: Utilitaire pour rechercher des agents dans la plateforme JADE.
- **ConsoleManager**: Gestionnaire de l'interface console avec effets visuels.

## Dépannage

### Problèmes de connexion avec le conteneur principal

Si les agents ne parviennent pas à se connecter au conteneur principal, vérifiez que :
- Le conteneur principal est bien démarré
- L'adresse IP spécifiée est correcte
- Aucun pare-feu ne bloque la communication sur le port 1099 (port par défaut de JADE)

```bash
# Pour vérifier si le port est ouvert sur Linux
netstat -an | grep 1099

# Pour vérifier sur Windows
netstat -an | findstr 1099
```

### Problèmes de communication avec le système expert

Si l'agent médecin ne parvient pas à communiquer avec le système expert :
- Vérifiez que le serveur API est bien démarré
- Vérifiez l'URL configurée
- Assurez-vous qu'aucun pare-feu ne bloque la communication

### Messages non affichés ou décalés

Si les messages ne s'affichent pas correctement ou avec du retard :
- Vérifiez que les comportements d'écoute sont correctement configurés
- Vérifiez la console du médecin pour voir si les messages sont bien reçus
- Redémarrez l'application si nécessaire

### Erreurs JADE

Pour les erreurs liées à JADE, consultez les logs pour plus de détails. Les erreurs courantes incluent :
- Problèmes d'enregistrement des agents dans le service DF
- Problèmes de noms d'agent en double
- Erreurs de communication ACL

## Extensions possibles

Le système peut être étendu de plusieurs façons :

1. **Interface graphique** : Remplacer l'interface console par une interface graphique Swing ou JavaFX.
2. **Historique des consultations** : Ajouter la possibilité de sauvegarder l'historique des consultations.
3. **Authentification** : Ajouter un système d'authentification pour les patients et les médecins.
4. **Plusieurs médecins** : Permettre à plusieurs médecins de s'enregistrer avec différentes spécialités.
5. **Mode hors ligne** : Ajouter la possibilité de fonctionner sans connexion constante au système expert.

## Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.

---

## Guide de développement

### Ajout de nouvelles fonctionnalités

Pour ajouter de nouvelles fonctionnalités, suivez ces étapes :

1. **Identifiez le composant concerné** :
    - S'il s'agit d'une nouvelle fonctionnalité pour le médecin, modifiez les classes dans le package `medecin`
    - S'il s'agit d'une fonctionnalité pour le patient, travaillez dans le package `patient`

2. **Créez de nouveaux comportements** :
    - Les comportements JADE sont la base de toute fonctionnalité
    - Étendez `CyclicBehaviour`, `OneShotBehaviour` ou `TickerBehaviour` selon vos besoins

3. **Mettez à jour les modèles** :
    - Si votre fonctionnalité nécessite de nouvelles données, ajoutez-les aux modèles existants
    - Ou créez de nouveaux modèles dans le package `models`

4. **Testez votre code** :
    - Testez d'abord en local (tous les agents sur la même machine)
    - Puis testez en environnement distribué si nécessaire

### Personnalisation de l'interface console

L'interface console peut être personnalisée en modifiant les méthodes de la classe `ConsoleManager` :

```java
// Modifier les couleurs
ConsoleManager.RED = "\u001B[38;5;196m"; // Rouge plus vif
ConsoleManager.GREEN = "\u001B[38;5;46m"; // Vert plus vif

// Ajouter de nouvelles méthodes d'affichage
public static void printHighlight(String message) {
    System.out.println(YELLOW + BOLD + "🔍 " + message + RESET);
}
```

### Débogage

Pour déboguer efficacement l'application, vous pouvez activer le mode debug de JADE :

```bash
java -Djade.util.Logger.properties=jade_logging.properties -cp "lib/*:target/jade-medical-consultation-1.0.0.jar" org.medecine.jade.MainContainer
```

Et créer un fichier `jade_logging.properties` avec le contenu suivant :

```properties
handlers=java.util.logging.ConsoleHandler
.level=INFO
jade.level=FINE
jade.core.level=FINE
```

Cela affichera plus d'informations sur le fonctionnement interne de JADE.


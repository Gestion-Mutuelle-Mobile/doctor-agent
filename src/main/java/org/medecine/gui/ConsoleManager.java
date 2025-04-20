package org.medecine.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Gestionnaire de console pour afficher des messages stylisés
 */
public class ConsoleManager {
    // Codes ANSI pour les couleurs
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Codes ANSI pour le style
    public static final String BOLD = "\u001B[1m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";

    public static void clearScreen() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Si la méthode échoue, imprimer plusieurs lignes vides comme alternative
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    public static void printSeparator() {
        System.out.println(CYAN + "────────────────────────────────────────────────────────────" + RESET);
    }

    public static void printHeader(String title) {
        printSeparator();
        System.out.println(BOLD + CYAN + "  " + title + RESET);
        printSeparator();
    }

    public static void printInfo(String message) {
        System.out.println(BLUE + "ℹ️  " + message + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "✅ " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠️  " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "❌ " + message + RESET);
    }

    public static void printPatientMessage(String patient, String message) {
        System.out.println(GREEN + BOLD + "👤 " + patient + ": " + RESET + GREEN + message + RESET);
    }

    public static void printDoctorMessage(String doctor, String message) {
        System.out.println(BLUE + BOLD + "👨‍⚕️ " + doctor + ": " + RESET + BLUE + message + RESET);
    }

    public static void printSystem(String message) {
        System.out.println(PURPLE + "🔄 " + message + RESET);
    }

    public static void printTypingEffect(String text, int delayMillis) {
        for (char c : text.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.print(text.substring(text.indexOf(c)));
                break;
            }
        }
        System.out.println();
    }

    public static void printLoadingDots(String prefix, int count, int delayMillis) {
        System.out.print(ITALIC + prefix);
        for (int i = 0; i < count; i++) {
            System.out.print(".");
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println(RESET);
    }

    public static void printAsciiArt(String resourcePath) {
        try (InputStream is = ConsoleManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(CYAN + line + RESET);
            }
        } catch (IOException e) {
            // Ignorer silencieusement
        }
    }
}
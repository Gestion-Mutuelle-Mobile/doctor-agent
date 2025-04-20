package org.medecine.utils;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utilitaires pour la gestion des entrées console
 */
public class ConsoleUtils {
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Lit une ligne depuis la console avec un timeout
     */
    public static String readLineWithTimeout(String prompt, int timeoutSeconds, String defaultValue) {
        System.out.print(prompt);

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> scanner.nextLine());

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println("\nTemps écoulé, utilisation de la valeur par défaut: " + defaultValue);
            // Consommer l'entrée éventuellement saisie après le timeout
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            return defaultValue;
        }
    }

    /**
     * Lit une ligne depuis la console
     */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Lit un nombre entier depuis la console
     */
    public static int readInt(String prompt, int defaultValue) {
        System.out.print(prompt);
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Format invalide, utilisation de la valeur par défaut: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Lit une réponse oui/non depuis la console
     */
    public static boolean readYesNo(String prompt, boolean defaultValue) {
        String defaultStr = defaultValue ? "O" : "N";
        String options = defaultValue ? "[O/n]" : "[o/N]";

        System.out.print(prompt + " " + options + ": ");
        String input = scanner.nextLine().trim().toUpperCase();

        if (input.isEmpty()) {
            return defaultValue;
        }

        return input.startsWith("O") || input.startsWith("Y");
    }
}
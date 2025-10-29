package org.shared;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Utils {
    public static void printError(String errorMsg) {
        System.out.println("\033[0;31m");
        System.out.println("=".repeat(errorMsg.length() / 2 - 2) + "ERROR" + "=".repeat(errorMsg.length() / 2 - 2));
        System.out.println(errorMsg + "\033[0m");
    }

    public enum Color {
        GREEN(32),
        RED(31);

        private final int colorCode;

        private Color(int colorCode) {
            this.colorCode = colorCode;
        }

        @Override
        public String toString() {
            return Integer.toString(colorCode);
        }
    }

    public static void printColored(String msg, Color c) {
        System.out.print("\033[0;%dm".formatted(c.colorCode));
        System.out.println(msg);
        System.out.print("\033[0m");
    }

    public static void printColoredInformation(String msg, Color c) {
        System.out.println("\033[0;%dm".formatted(c.colorCode));
        System.out.println("=".repeat(msg.length() / 2 - 2) + "Information" + "=".repeat(msg.length() / 2 - 2));
        System.out.println(msg + "\033[0m");
    }

    public static int promptInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                var i = scanner.nextInt();
                scanner.nextLine(); // remove remaining newline
                return i;
            } catch (InputMismatchException e) {
                scanner.nextLine();
                printError("Invalid input.");
            }
        }
    }

    public static boolean promptBoolean(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                switch (scanner.nextLine().toLowerCase()) {
                    case "y", "yes", "" -> {
                        return true;
                    }
                    case "n", "no" -> {
                        return false;
                    }
                    default -> {
                        throw new InputMismatchException();
                    }
                }
            } catch (InputMismatchException e) {
                scanner.nextLine();
                printError("Invalid input.");
            }
        }

    }

}

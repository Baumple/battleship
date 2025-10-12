package org.shared;

class Utils {
    public static void printError(String errorMsg) {
        System.out.println("\033[0;31m");
        System.out.println("=".repeat(errorMsg.length() / 2 - 2) + "ERROR" + "=".repeat(errorMsg.length() / 2 - 2));
        System.out.println(errorMsg + "\033[0m");
    }
}

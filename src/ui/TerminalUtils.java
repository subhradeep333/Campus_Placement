package ui;

import model.ApplicationStatus;

public class TerminalUtils {
    // ANSI Colors
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_MAGENTA = "\u001B[45m";

    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printHeader(String title) {
        int width = 70;
        String border = "═".repeat(width);
        System.out.println(CYAN + BOLD + "╔" + border + "╗" + RESET);
        int padding = (width - title.length()) / 2;
        String paddedTitle = " ".repeat(Math.max(0, padding)) + title;
        paddedTitle += " ".repeat(Math.max(0, width - paddedTitle.length()));
        System.out.println(CYAN + BOLD + "║" + paddedTitle + "║" + RESET);
        System.out.println(CYAN + BOLD + "╚" + border + "╝" + RESET);
    }

    public static void printSubHeader(String title) {
        System.out.println("\n" + MAGENTA + BOLD + "─── [ " + title + " ] " + "─".repeat(Math.max(0, 50 - title.length())) + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + BOLD + "✔ " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + BOLD + "✖ " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(CYAN + "ℹ " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }

    public static String getStatusBadge(ApplicationStatus status) {
        switch (status) {
            case ACCEPTED:
                return GREEN + BOLD + "[ ACCEPTED 🎉 ]" + RESET;
            case SHORTLISTED:
                return CYAN + BOLD + "[ SHORTLISTED ⭐ ]" + RESET;
            case REJECTED:
                return RED + BOLD + "[ REJECTED ✖ ]" + RESET;
            case PENDING:
            default:
                return YELLOW + BOLD + "[ PENDING ⏳ ]" + RESET;
        }
    }

    public static void printDivider() {
        System.out.println(WHITE + "───────────────────────────────────────────────────────────────────────" + RESET);
    }
}

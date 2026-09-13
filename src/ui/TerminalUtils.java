package ui;

import model.ApplicationStatus;

public final class TerminalUtils {
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

    // Precomputed UI Constants & Borders
    private static final int HEADER_WIDTH = 70;
    private static final String HEADER_BORDER = "═".repeat(HEADER_WIDTH);
    private static final String DIVIDER = WHITE + "───────────────────────────────────────────────────────────────────────" + RESET;

    // Precomputed Status Badges
    private static final String BADGE_ACCEPTED = GREEN + BOLD + "[ ACCEPTED 🎉 ]" + RESET;
    private static final String BADGE_SHORTLISTED = CYAN + BOLD + "[ SHORTLISTED ⭐ ]" + RESET;
    private static final String BADGE_REJECTED = RED + BOLD + "[ REJECTED ✖ ]" + RESET;
    private static final String BADGE_PENDING = YELLOW + BOLD + "[ PENDING ⏳ ]" + RESET;

    // Prevent instantiation
    private TerminalUtils() {}

    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printHeader(String title) {
        System.out.println(CYAN + BOLD + "╔" + HEADER_BORDER + "╗" + RESET);
        int padding = Math.max(0, (HEADER_WIDTH - title.length()) / 2);
        int trailing = Math.max(0, HEADER_WIDTH - padding - title.length());
        System.out.println(CYAN + BOLD + "║" + " ".repeat(padding) + title + " ".repeat(trailing) + "║" + RESET);
        System.out.println(CYAN + BOLD + "╚" + HEADER_BORDER + "╝" + RESET);
    }

    public static void printSubHeader(String title) {
        int trailingDashes = Math.max(0, 50 - title.length());
        System.out.println("\n" + MAGENTA + BOLD + "─── [ " + title + " ] " + "─".repeat(trailingDashes) + RESET);
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
        if (status == null) return BADGE_PENDING;
        return switch (status) {
            case ACCEPTED -> BADGE_ACCEPTED;
            case SHORTLISTED -> BADGE_SHORTLISTED;
            case REJECTED -> BADGE_REJECTED;
            case PENDING -> BADGE_PENDING;
        };
    }

    public static void printDivider() {
        System.out.println(DIVIDER);
    }
}

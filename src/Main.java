import service.AuthService;
import service.DataStore;
import service.PersistenceService;
import service.RecruitmentService;
import ui.ConsoleUI;
import web.WebServer;

public class Main {
    public static void main(String[] args) {
        // Ensure data directory exists
        PersistenceService.ensureDataDirectoryExists();

        // Load persisted data or initialize new store
        DataStore dataStore = PersistenceService.loadDataStore();

        // Initialize Services
        AuthService authService = new AuthService(dataStore);
        RecruitmentService recruitmentService = new RecruitmentService(dataStore);

        // Auto-seed demo data if store is empty
        if (dataStore.getCompanies().isEmpty() && dataStore.getStudents().isEmpty()) {
            recruitmentService.seedDemoData();
        }

        // Start Web Server on port 8080 (Background Thread)
        WebServer webServer = new WebServer(8080, authService, recruitmentService);
        Thread webThread = new Thread(webServer::start);
        webThread.setDaemon(true);
        webThread.start();

        // Start Interactive Console UI
        ConsoleUI consoleUI = new ConsoleUI(authService, recruitmentService);
        consoleUI.start();
    }
}

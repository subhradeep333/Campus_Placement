package service;

import model.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PersistenceService {
    private static final String DATA_DIR = "data";
    private static final String BIN_DATAFILE = DATA_DIR + File.separator + "placement_datastore.bin";
    private static DatabaseService databaseService = new DatabaseService();

    public static void ensureDataDirectoryExists() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void saveDataStore(DataStore dataStore) {
        ensureDataDirectoryExists();

        // 1. Save to SQLite Database
        databaseService.syncDataStoreToDb(dataStore);

        // 2. Save binary backup file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BIN_DATAFILE))) {
            oos.writeObject(dataStore);
        } catch (IOException ignored) {}

        // 3. Export readable JSON summary files
        exportHumanReadableData(dataStore);
    }

    public static DataStore loadDataStore() {
        ensureDataDirectoryExists();

        // Load from SQLite DB first
        DataStore dsFromDb = databaseService.loadDataStoreFromDb();
        if (!dsFromDb.getStudents().isEmpty() || !dsFromDb.getCompanies().isEmpty()) {
            return dsFromDb;
        }

        // Fallback to binary serialization if DB is empty
        File binFile = new File(BIN_DATAFILE);
        if (binFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(binFile))) {
                DataStore ds = (DataStore) ois.readObject();
                databaseService.syncDataStoreToDb(ds);
                return ds;
            } catch (Exception ignored) {}
        }

        return new DataStore();
    }

    private static void exportHumanReadableData(DataStore dataStore) {
        try {
            // Students JSON
            StringBuilder sbStudents = new StringBuilder("[\n");
            List<Student> students = dataStore.getStudents();
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                sbStudents.append("  {\n")
                        .append("    \"id\": \"").append(s.getId()).append("\",\n")
                        .append("    \"name\": \"").append(s.getFullName()).append("\",\n")
                        .append("    \"email\": \"").append(s.getEmail()).append("\",\n")
                        .append("    \"branch\": \"").append(s.getBranch()).append("\",\n")
                        .append("    \"cgpa\": ").append(s.getCgpa()).append(",\n")
                        .append("    \"skills\": \"").append(escapeJson(s.getSkills())).append("\",\n")
                        .append("    \"hasCvSubmitted\": ").append(s.isHasSubmittedCv()).append("\n")
                        .append("  }").append(i < students.size() - 1 ? "," : "").append("\n");
            }
            sbStudents.append("]");
            Files.writeString(Paths.get(DATA_DIR, "students.json"), sbStudents.toString());

            // Companies JSON
            StringBuilder sbCompanies = new StringBuilder("[\n");
            List<Company> companies = dataStore.getCompanies();
            for (int i = 0; i < companies.size(); i++) {
                Company c = companies.get(i);
                sbCompanies.append("  {\n")
                        .append("    \"id\": \"").append(c.getId()).append("\",\n")
                        .append("    \"companyName\": \"").append(c.getCompanyName()).append("\",\n")
                        .append("    \"email\": \"").append(c.getEmail()).append("\",\n")
                        .append("    \"industry\": \"").append(c.getIndustry()).append("\",\n")
                        .append("    \"location\": \"").append(c.getLocation()).append("\"\n")
                        .append("  }").append(i < companies.size() - 1 ? "," : "").append("\n");
            }
            sbCompanies.append("]");
            Files.writeString(Paths.get(DATA_DIR, "companies.json"), sbCompanies.toString());

            // Jobs JSON
            StringBuilder sbJobs = new StringBuilder("[\n");
            List<JobPosting> jobs = dataStore.getJobPostings();
            for (int i = 0; i < jobs.size(); i++) {
                JobPosting j = jobs.get(i);
                sbJobs.append("  {\n")
                        .append("    \"id\": \"").append(j.getId()).append("\",\n")
                        .append("    \"title\": \"").append(j.getTitle()).append("\",\n")
                        .append("    \"companyName\": \"").append(j.getCompanyName()).append("\",\n")
                        .append("    \"packageLpa\": ").append(j.getPackageLpa()).append(",\n")
                        .append("    \"minCgpa\": ").append(j.getMinCgpa()).append(",\n")
                        .append("    \"active\": ").append(j.isActive()).append("\n")
                        .append("  }").append(i < jobs.size() - 1 ? "," : "").append("\n");
            }
            sbJobs.append("]");
            Files.writeString(Paths.get(DATA_DIR, "jobs.json"), sbJobs.toString());

            // Applications JSON
            StringBuilder sbApps = new StringBuilder("[\n");
            List<Application> apps = dataStore.getApplications();
            for (int i = 0; i < apps.size(); i++) {
                Application a = apps.get(i);
                sbApps.append("  {\n")
                        .append("    \"id\": \"").append(a.getId()).append("\",\n")
                        .append("    \"jobTitle\": \"").append(a.getJobTitle()).append("\",\n")
                        .append("    \"studentName\": \"").append(a.getStudentName()).append("\",\n")
                        .append("    \"companyName\": \"").append(a.getCompanyName()).append("\",\n")
                        .append("    \"status\": \"").append(a.getStatus()).append("\"\n")
                        .append("  }").append(i < apps.size() - 1 ? "," : "").append("\n");
            }
            sbApps.append("]");
            Files.writeString(Paths.get(DATA_DIR, "applications.json"), sbApps.toString());

        } catch (Exception ignored) {}
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}

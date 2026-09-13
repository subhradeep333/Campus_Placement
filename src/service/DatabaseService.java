package service;

import model.*;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final String DB_DIR = "data";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIR + File.separator + "placement_portal.db";

    public DatabaseService() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[Database Error] SQLite JDBC Driver not found: " + e.getMessage());
        }
        ensureDatabaseInitialized();
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(DB_URL);
    }

    private void ensureDatabaseInitialized() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Enable Foreign Keys in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Students Table
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(30), " +
                    "branch VARCHAR(50), " +
                    "cgpa REAL, " +
                    "skills TEXT, " +
                    "cv_summary TEXT, " +
                    "cv_file_path TEXT, " +
                    "has_submitted_cv INTEGER DEFAULT 0" +
                    ");");

            // Companies Table
            stmt.execute("CREATE TABLE IF NOT EXISTS companies (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "company_name VARCHAR(100) NOT NULL, " +
                    "industry VARCHAR(100), " +
                    "location VARCHAR(100), " +
                    "website VARCHAR(150), " +
                    "description TEXT" +
                    ");");

            // Job Postings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS job_postings (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "company_id VARCHAR(50) NOT NULL, " +
                    "company_name VARCHAR(100) NOT NULL, " +
                    "title VARCHAR(150) NOT NULL, " +
                    "description TEXT, " +
                    "required_skills TEXT, " +
                    "package_lpa REAL, " +
                    "min_cgpa REAL, " +
                    "posted_date VARCHAR(50), " +
                    "is_active INTEGER DEFAULT 1, " +
                    "FOREIGN KEY (company_id) REFERENCES companies(id)" +
                    ");");

            // Applications Table
            stmt.execute("CREATE TABLE IF NOT EXISTS applications (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "job_id VARCHAR(50) NOT NULL, " +
                    "job_title VARCHAR(150) NOT NULL, " +
                    "student_id VARCHAR(50) NOT NULL, " +
                    "student_name VARCHAR(100) NOT NULL, " +
                    "student_branch VARCHAR(50), " +
                    "student_cgpa REAL, " +
                    "company_id VARCHAR(50) NOT NULL, " +
                    "company_name VARCHAR(100) NOT NULL, " +
                    "status VARCHAR(30) NOT NULL, " +
                    "applied_date VARCHAR(50), " +
                    "FOREIGN KEY (job_id) REFERENCES job_postings(id), " +
                    "FOREIGN KEY (student_id) REFERENCES students(id), " +
                    "FOREIGN KEY (company_id) REFERENCES companies(id)" +
                    ");");

        } catch (SQLException e) {
            System.err.println("[Database Error] Initialization failed: " + e.getMessage());
        }
    }

    // Load full state from SQL database into DataStore
    public DataStore loadDataStoreFromDb() {
        DataStore dataStore = new DataStore();
        try (Connection conn = getConnection()) {
            // Load Students
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {
                while (rs.next()) {
                    Student s = new Student(
                            rs.getString("id"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("phone"),
                            rs.getString("branch"),
                            rs.getDouble("cgpa")
                    );
                    s.setSkills(rs.getString("skills"));
                    s.setCvSummary(rs.getString("cv_summary"));
                    s.setCvFilePath(rs.getString("cv_file_path"));
                    s.setHasSubmittedCv(rs.getInt("has_submitted_cv") == 1);
                    dataStore.addStudent(s);
                }
            }

            // Load Companies
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM companies")) {
                while (rs.next()) {
                    Company c = new Company(
                            rs.getString("id"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("company_name"),
                            rs.getString("industry"),
                            rs.getString("location"),
                            rs.getString("website"),
                            rs.getString("description")
                    );
                    dataStore.addCompany(c);
                }
            }

            // Load Jobs
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM job_postings")) {
                while (rs.next()) {
                    JobPosting j = new JobPosting(
                            rs.getString("id"),
                            rs.getString("company_id"),
                            rs.getString("company_name"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("required_skills"),
                            rs.getDouble("package_lpa"),
                            rs.getDouble("min_cgpa")
                    );
                    j.setPostedDate(rs.getString("posted_date"));
                    j.setActive(rs.getInt("is_active") == 1);
                    dataStore.addJobPosting(j);
                }
            }

            // Load Applications
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM applications")) {
                while (rs.next()) {
                    Application a = new Application(
                            rs.getString("id"),
                            rs.getString("job_id"),
                            rs.getString("job_title"),
                            rs.getString("student_id"),
                            rs.getString("student_name"),
                            rs.getString("student_branch"),
                            rs.getDouble("student_cgpa"),
                            rs.getString("company_id"),
                            rs.getString("company_name")
                    );
                    a.setStatus(ApplicationStatus.valueOf(rs.getString("status")));
                    a.setAppliedDate(rs.getString("applied_date"));
                    dataStore.addApplication(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Error loading data from SQL DB: " + e.getMessage());
        }
        return dataStore;
    }

    // Save full DataStore state to SQL database
    public void syncDataStoreToDb(DataStore dataStore) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Sync Students
            String upsertStudentSql = "INSERT OR REPLACE INTO students " +
                    "(id, email, password, full_name, phone, branch, cgpa, skills, cv_summary, cv_file_path, has_submitted_cv) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(upsertStudentSql)) {
                for (Student s : dataStore.getStudents()) {
                    pstmt.setString(1, s.getId());
                    pstmt.setString(2, s.getEmail());
                    pstmt.setString(3, s.getPassword());
                    pstmt.setString(4, s.getFullName());
                    pstmt.setString(5, s.getPhone());
                    pstmt.setString(6, s.getBranch());
                    pstmt.setDouble(7, s.getCgpa());
                    pstmt.setString(8, s.getSkills());
                    pstmt.setString(9, s.getCvSummary());
                    pstmt.setString(10, s.getCvFilePath());
                    pstmt.setInt(11, s.isHasSubmittedCv() ? 1 : 0);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // Sync Companies
            String upsertCompanySql = "INSERT OR REPLACE INTO companies " +
                    "(id, email, password, company_name, industry, location, website, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(upsertCompanySql)) {
                for (Company c : dataStore.getCompanies()) {
                    pstmt.setString(1, c.getId());
                    pstmt.setString(2, c.getEmail());
                    pstmt.setString(3, c.getPassword());
                    pstmt.setString(4, c.getCompanyName());
                    pstmt.setString(5, c.getIndustry());
                    pstmt.setString(6, c.getLocation());
                    pstmt.setString(7, c.getWebsite());
                    pstmt.setString(8, c.getDescription());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // Sync Job Postings
            String upsertJobSql = "INSERT OR REPLACE INTO job_postings " +
                    "(id, company_id, company_name, title, description, required_skills, package_lpa, min_cgpa, posted_date, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(upsertJobSql)) {
                for (JobPosting j : dataStore.getJobPostings()) {
                    pstmt.setString(1, j.getId());
                    pstmt.setString(2, j.getCompanyId());
                    pstmt.setString(3, j.getCompanyName());
                    pstmt.setString(4, j.getTitle());
                    pstmt.setString(5, j.getDescription());
                    pstmt.setString(6, j.getRequiredSkills());
                    pstmt.setDouble(7, j.getPackageLpa());
                    pstmt.setDouble(8, j.getMinCgpa());
                    pstmt.setString(9, j.getPostedDate());
                    pstmt.setInt(10, j.isActive() ? 1 : 0);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // Sync Applications
            String upsertAppSql = "INSERT OR REPLACE INTO applications " +
                    "(id, job_id, job_title, student_id, student_name, student_branch, student_cgpa, company_id, company_name, status, applied_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(upsertAppSql)) {
                for (Application a : dataStore.getApplications()) {
                    pstmt.setString(1, a.getId());
                    pstmt.setString(2, a.getJobId());
                    pstmt.setString(3, a.getJobTitle());
                    pstmt.setString(4, a.getStudentId());
                    pstmt.setString(5, a.getStudentName());
                    pstmt.setString(6, a.getStudentBranch());
                    pstmt.setDouble(7, a.getStudentCgpa());
                    pstmt.setString(8, a.getCompanyId());
                    pstmt.setString(9, a.getCompanyName());
                    pstmt.setString(10, a.getStatus().name());
                    pstmt.setString(11, a.getAppliedDate());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            System.err.println("[Database Error] Error syncing data to SQL DB: " + e.getMessage());
        }
    }
}

package service;

import model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseService {
    private static final String CONFIG_FILE = "db.properties";
    private static final String DB_DIR = "data";
    
    private String dbType = "mysql";
    private String dbUrl = "jdbc:mysql://localhost:3306/placement_portal?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private String dbUser = "root";
    private String dbPassword = "root";
    private String driverClass = "com.mysql.cj.jdbc.Driver";

    private String sqliteUrl = "jdbc:sqlite:" + DB_DIR + File.separator + "placement_portal.db";
    private boolean usingSqliteFallback = false;

    public DatabaseService() {
        loadDatabaseConfig();
        initDriver();
        ensureDatabaseInitialized();
    }

    private void loadDatabaseConfig() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                props.load(input);
                this.dbType = props.getProperty("db.type", "mysql").trim().toLowerCase();
                this.dbUser = props.getProperty("db.user", "root").trim();
                this.dbPassword = props.getProperty("db.password", "root").trim();

                if ("mysql".equals(dbType)) {
                    this.driverClass = props.getProperty("db.mysql.driver", "com.mysql.cj.jdbc.Driver").trim();
                    this.dbUrl = props.getProperty("db.mysql.url", 
                        "jdbc:mysql://localhost:3306/placement_portal?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC").trim();
                } else {
                    this.driverClass = props.getProperty("db.sqlite.driver", "org.sqlite.JDBC").trim();
                    this.dbUrl = props.getProperty("db.sqlite.url", sqliteUrl).trim();
                }
            } catch (Exception e) {
                System.err.println("[Database Config] Warning loading db.properties: " + e.getMessage());
            }
        }
    }

    private void initDriver() {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            System.err.println("[Database Driver] Warning: Driver " + driverClass + " not found: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        if (usingSqliteFallback || "sqlite".equals(dbType)) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ignored) {}
            return DriverManager.getConnection(sqliteUrl);
        }

        try {
            Class.forName(driverClass);
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (Exception e) {
            System.err.println("[Database Alert] Could not connect to MySQL server (" + e.getMessage() + "). Falling back to embedded SQLite database.");
            this.usingSqliteFallback = true;
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ignored) {}
            return DriverManager.getConnection(sqliteUrl);
        }
    }

    private void ensureDatabaseInitialized() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if (usingSqliteFallback || "sqlite".equals(dbType)) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }

            // Students Table
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(30), " +
                    "branch VARCHAR(50), " +
                    "cgpa DOUBLE, " +
                    "skills TEXT, " +
                    "cv_summary TEXT, " +
                    "cv_file_path TEXT, " +
                    "has_submitted_cv INT DEFAULT 0" +
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
                    "package_lpa DOUBLE, " +
                    "min_cgpa DOUBLE, " +
                    "posted_date VARCHAR(50), " +
                    "is_active INT DEFAULT 1" +
                    ");");

            // Applications Table
            stmt.execute("CREATE TABLE IF NOT EXISTS applications (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "job_id VARCHAR(50) NOT NULL, " +
                    "job_title VARCHAR(150) NOT NULL, " +
                    "student_id VARCHAR(50) NOT NULL, " +
                    "student_name VARCHAR(100) NOT NULL, " +
                    "student_branch VARCHAR(50), " +
                    "student_cgpa DOUBLE, " +
                    "company_id VARCHAR(50) NOT NULL, " +
                    "company_name VARCHAR(100) NOT NULL, " +
                    "status VARCHAR(30) NOT NULL, " +
                    "applied_date VARCHAR(50)" +
                    ");");

            // Interviews Table
            stmt.execute("CREATE TABLE IF NOT EXISTS interviews (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "application_id VARCHAR(50) NOT NULL, " +
                    "job_id VARCHAR(50) NOT NULL, " +
                    "job_title VARCHAR(150) NOT NULL, " +
                    "student_id VARCHAR(50) NOT NULL, " +
                    "student_name VARCHAR(100) NOT NULL, " +
                    "company_id VARCHAR(50) NOT NULL, " +
                    "company_name VARCHAR(100) NOT NULL, " +
                    "round_type VARCHAR(100) NOT NULL, " +
                    "round_number INT NOT NULL, " +
                    "scheduled_datetime VARCHAR(50) NOT NULL, " +
                    "location_or_link TEXT, " +
                    "interviewer_name VARCHAR(100), " +
                    "status VARCHAR(30) NOT NULL, " +
                    "feedback TEXT, " +
                    "score DOUBLE DEFAULT 0.0" +
                    ");");

            System.out.println("✅ Database Engine Initialized! Mode: " + (usingSqliteFallback ? "SQLite (Fallback)" : dbType.toUpperCase()));
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

            // Load Interviews
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM interviews")) {
                while (rs.next()) {
                    InterviewRound ir = new InterviewRound(
                            rs.getString("id"),
                            rs.getString("application_id"),
                            rs.getString("job_id"),
                            rs.getString("job_title"),
                            rs.getString("student_id"),
                            rs.getString("student_name"),
                            rs.getString("company_id"),
                            rs.getString("company_name"),
                            rs.getString("round_type"),
                            rs.getInt("round_number"),
                            rs.getString("scheduled_datetime"),
                            rs.getString("location_or_link"),
                            rs.getString("interviewer_name")
                    );
                    ir.setStatus(InterviewStatus.valueOf(rs.getString("status")));
                    ir.setFeedback(rs.getString("feedback"));
                    ir.setScore(rs.getDouble("score"));
                    dataStore.addInterview(ir);
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
            String upsertStudentSql = "REPLACE INTO students " +
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
            String upsertCompanySql = "REPLACE INTO companies " +
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
            String upsertJobSql = "REPLACE INTO job_postings " +
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
            String upsertAppSql = "REPLACE INTO applications " +
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

            // Sync Interviews
            String upsertInterviewSql = "REPLACE INTO interviews " +
                    "(id, application_id, job_id, job_title, student_id, student_name, company_id, company_name, round_type, round_number, scheduled_datetime, location_or_link, interviewer_name, status, feedback, score) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(upsertInterviewSql)) {
                for (InterviewRound i : dataStore.getInterviews()) {
                    pstmt.setString(1, i.getId());
                    pstmt.setString(2, i.getApplicationId());
                    pstmt.setString(3, i.getJobId());
                    pstmt.setString(4, i.getJobTitle());
                    pstmt.setString(5, i.getStudentId());
                    pstmt.setString(6, i.getStudentName());
                    pstmt.setString(7, i.getCompanyId());
                    pstmt.setString(8, i.getCompanyName());
                    pstmt.setString(9, i.getRoundType());
                    pstmt.setInt(10, i.getRoundNumber());
                    pstmt.setString(11, i.getScheduledDateTime());
                    pstmt.setString(12, i.getLocationOrLink());
                    pstmt.setString(13, i.getInterviewerName());
                    pstmt.setString(14, i.getStatus().name());
                    pstmt.setString(15, i.getFeedback());
                    pstmt.setDouble(16, i.getScore());
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

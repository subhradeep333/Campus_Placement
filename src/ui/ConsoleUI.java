package ui;

import model.*;
import service.*;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUI {
    private AuthService authService;
    private RecruitmentService recruitmentService;
    private Scanner scanner;

    public ConsoleUI(AuthService authService, RecruitmentService recruitmentService) {
        this.authService = authService;
        this.recruitmentService = recruitmentService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            TerminalUtils.printHeader("CAMPUS RECRUITMENT & PLACEMENT PORTAL");
            System.out.println(TerminalUtils.BOLD + "Welcome! Select an option from the menu below:" + TerminalUtils.RESET);
            System.out.println("  1. 🔑 Login (Student or Company)");
            System.out.println("  2. 🎓 Register as a Student");
            System.out.println("  3. 🏢 Register as a Company");
            System.out.println("  4. 🌐 Browse Companies & Job Listings (Guest)");
            System.out.println("  5. ⚡ Load Demo Sample Data (Companies, Students, Jobs)");
            System.out.println("  0. ❌ Exit");
            TerminalUtils.printDivider();
            System.out.print(TerminalUtils.CYAN + "Enter choice [0-5]: " + TerminalUtils.RESET);

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleStudentRegistration();
                    break;
                case "3":
                    handleCompanyRegistration();
                    break;
                case "4":
                    handleGuestBrowse();
                    break;
                case "5":
                    recruitmentService.seedDemoData();
                    TerminalUtils.printSuccess("Demo sample data loaded successfully! You can now login using demo accounts.");
                    System.out.println("  • Student Demo Login: " + TerminalUtils.CYAN + "alex@university.edu" + TerminalUtils.RESET + " / Password: " + TerminalUtils.CYAN + "pass123" + TerminalUtils.RESET);
                    System.out.println("  • Company Demo Login: " + TerminalUtils.CYAN + "recruitment@google.com" + TerminalUtils.RESET + " / Password: " + TerminalUtils.CYAN + "google123" + TerminalUtils.RESET);
                    promptEnterKey();
                    break;
                case "0":
                    running = false;
                    TerminalUtils.printInfo("Thank you for using Campus Recruitment Portal. Goodbye!");
                    break;
                default:
                    TerminalUtils.printError("Invalid choice! Please enter a number between 0 and 5.");
                    promptEnterKey();
                    break;
            }
        }
    }

    // --- Authentication Handlers ---

    private void handleLogin() {
        TerminalUtils.printSubHeader("ACCOUNT LOGIN");
        System.out.print("Email Address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = authService.login(email, password);
        if (user == null) {
            TerminalUtils.printError("Invalid email or password!");
            promptEnterKey();
            return;
        }

        if (user instanceof Student) {
            Student student = (Student) user;
            TerminalUtils.printSuccess("Welcome back, " + student.getFullName() + "! (Student Portal)");
            promptEnterKey();
            studentDashboard(student);
        } else if (user instanceof Company) {
            Company company = (Company) user;
            TerminalUtils.printSuccess("Welcome back, " + company.getCompanyName() + "! (Company Portal)");
            promptEnterKey();
            companyDashboard(company);
        }
    }

    private void handleStudentRegistration() {
        TerminalUtils.printSubHeader("STUDENT REGISTRATION");
        try {
            System.out.print("Full Name: ");
            String fullName = scanner.nextLine().trim();
            if (fullName.isEmpty()) throw new IllegalArgumentException("Name cannot be empty.");

            System.out.print("Email Address: ");
            String email = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (password.length() < 4) throw new IllegalArgumentException("Password must be at least 4 characters.");

            System.out.print("Phone Number: ");
            String phone = scanner.nextLine().trim();

            System.out.print("Branch/Department (e.g. CS, IT, ECE): ");
            String branch = scanner.nextLine().trim();

            System.out.print("CGPA (0.0 to 4.0 or 10.0): ");
            double cgpa = Double.parseDouble(scanner.nextLine().trim());

            Student student = authService.registerStudent(email, password, fullName, phone, branch, cgpa);
            TerminalUtils.printSuccess("Student registration successful! Your Student ID is: " + student.getId());
            promptEnterKey();
        } catch (NumberFormatException e) {
            TerminalUtils.printError("Invalid CGPA format! Please enter a valid decimal number.");
            promptEnterKey();
        } catch (Exception e) {
            TerminalUtils.printError(e.getMessage());
            promptEnterKey();
        }
    }

    private void handleCompanyRegistration() {
        TerminalUtils.printSubHeader("COMPANY REGISTRATION");
        try {
            System.out.print("Company Name: ");
            String companyName = scanner.nextLine().trim();

            System.out.print("Official Work Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            System.out.print("Industry / Domain (e.g., Software, FinTech, AI): ");
            String industry = scanner.nextLine().trim();

            System.out.print("Headquarters / Office Location: ");
            String location = scanner.nextLine().trim();

            System.out.print("Website URL: ");
            String website = scanner.nextLine().trim();

            System.out.print("Brief Company Description: ");
            String description = scanner.nextLine().trim();

            Company company = authService.registerCompany(email, password, companyName, industry, location, website, description);
            TerminalUtils.printSuccess("Company registration successful! Company ID: " + company.getId());
            promptEnterKey();
        } catch (Exception e) {
            TerminalUtils.printError(e.getMessage());
            promptEnterKey();
        }
    }

    // --- Student Portal Dashboard ---

    private void studentDashboard(Student student) {
        boolean inPortal = true;
        while (inPortal) {
            TerminalUtils.printHeader("STUDENT DASHBOARD - " + student.getFullName().toUpperCase());
            System.out.println("Student ID: " + TerminalUtils.CYAN + student.getId() + TerminalUtils.RESET +
                    " | Branch: " + student.getBranch() + " | CGPA: " + student.getCgpa());
            System.out.println("CV Status: " + (student.isHasSubmittedCv() ? TerminalUtils.GREEN + "✔ Submitted" + TerminalUtils.RESET : TerminalUtils.RED + "✖ Not Submitted" + TerminalUtils.RESET));
            TerminalUtils.printDivider();
            System.out.println("  1. 📄 Submit / Update My CV & Skills");
            System.out.println("  2. 👤 View My Profile & CV Details");
            System.out.println("  3. 🏢 View All Companies & Open Jobs");
            System.out.println("  4. 🔍 Search / Filter Job Openings");
            System.out.println("  5. 📝 Apply for a Job Opening");
            System.out.println("  6. 📊 Track My Job Application Statuses");
            System.out.println("  0. 🚪 Logout");
            TerminalUtils.printDivider();
            System.out.print(TerminalUtils.CYAN + "Enter option [0-6]: " + TerminalUtils.RESET);

            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    handleCvSubmission(student);
                    break;
                case "2":
                    viewStudentProfile(student);
                    break;
                case "3":
                    displayAllCompaniesAndJobs();
                    break;
                case "4":
                    searchJobsMenu();
                    break;
                case "5":
                    handleJobApplication(student);
                    break;
                case "6":
                    trackStudentApplications(student);
                    break;
                case "0":
                    authService.logout();
                    inPortal = false;
                    TerminalUtils.printInfo("Logged out of Student Portal.");
                    break;
                default:
                    TerminalUtils.printError("Invalid option.");
                    promptEnterKey();
                    break;
            }
        }
    }

    private void handleCvSubmission(Student student) {
        TerminalUtils.printSubHeader("SUBMIT / UPDATE CV & SKILLS");
        System.out.println("Enter key technical skills (comma separated, e.g. Java, Python, SQL, Git):");
        System.out.print("> ");
        String skills = scanner.nextLine().trim();

        System.out.println("Enter CV Summary (Highlight your education, achievements, and key projects):");
        System.out.print("> ");
        String cvSummary = scanner.nextLine().trim();

        System.out.println("Enter File Path to attached CV document (e.g., /path/to/resume.pdf or press ENTER to skip):");
        System.out.print("> ");
        String filePath = scanner.nextLine().trim();

        recruitmentService.updateStudentCv(student, skills, cvSummary, filePath);
        TerminalUtils.printSuccess("Your CV and Skills profile have been successfully updated!");
        promptEnterKey();
    }

    private void viewStudentProfile(Student student) {
        TerminalUtils.printSubHeader("MY CV & PROFILE");
        System.out.println(TerminalUtils.BOLD + "Name: " + TerminalUtils.RESET + student.getFullName());
        System.out.println(TerminalUtils.BOLD + "Email: " + TerminalUtils.RESET + student.getEmail());
        System.out.println(TerminalUtils.BOLD + "Phone: " + TerminalUtils.RESET + student.getPhone());
        System.out.println(TerminalUtils.BOLD + "Branch: " + TerminalUtils.RESET + student.getBranch());
        System.out.println(TerminalUtils.BOLD + "CGPA: " + TerminalUtils.RESET + student.getCgpa());
        System.out.println(TerminalUtils.BOLD + "Skills: " + TerminalUtils.RESET + student.getSkills());
        System.out.println(TerminalUtils.BOLD + "CV Document Path: " + TerminalUtils.RESET + (student.getCvFilePath().isEmpty() ? "None attached" : student.getCvFilePath()));
        System.out.println(TerminalUtils.BOLD + "CV Summary:" + TerminalUtils.RESET);
        System.out.println(student.getCvSummary().isEmpty() ? "  [No summary provided yet]" : "  " + student.getCvSummary());
        promptEnterKey();
    }

    private void displayAllCompaniesAndJobs() {
        TerminalUtils.printSubHeader("ALL REGISTERED COMPANIES & OPEN JOBS");
        List<Company> companies = recruitmentService.getAllCompanies();
        if (companies.isEmpty()) {
            TerminalUtils.printWarning("No registered companies in system yet.");
        } else {
            for (Company c : companies) {
                System.out.println(TerminalUtils.BOLD + "🏢 " + c.getCompanyName() + TerminalUtils.RESET + " (" + c.getIndustry() + " - " + c.getLocation() + ")");
                System.out.println("   Web: " + c.getWebsite() + " | Contact: " + c.getEmail());
                System.out.println("   About: " + c.getDescription());

                List<JobPosting> jobs = recruitmentService.getCompanyJobPostings(c.getId());
                if (jobs.isEmpty()) {
                    System.out.println("   └─ " + TerminalUtils.YELLOW + "No active job listings right now." + TerminalUtils.RESET);
                } else {
                    for (JobPosting j : jobs) {
                        if (j.isActive()) {
                            System.out.printf("   └─ [%s] %s | Package: %.1f LPA | Min CGPA: %.2f | Skills: %s%n",
                                    TerminalUtils.CYAN + j.getId() + TerminalUtils.RESET,
                                    TerminalUtils.BOLD + j.getTitle() + TerminalUtils.RESET,
                                    j.getPackageLpa(), j.getMinCgpa(), j.getRequiredSkills());
                        }
                    }
                }
                System.out.println();
            }
        }
        promptEnterKey();
    }

    private void searchJobsMenu() {
        TerminalUtils.printSubHeader("SEARCH / FILTER JOBS");
        System.out.print("Enter search keyword (Title, Skill, or Company Name): ");
        String keyword = scanner.nextLine().trim();

        List<JobPosting> matchedJobs = recruitmentService.filterJobsBySkillOrTitle(keyword);
        if (matchedJobs.isEmpty()) {
            TerminalUtils.printWarning("No job listings match your keyword: '" + keyword + "'");
        } else {
            TerminalUtils.printSuccess("Found " + matchedJobs.size() + " matching job postings:");
            for (JobPosting j : matchedJobs) {
                System.out.printf("[%s] %s @ %s | Package: %.1f LPA | Min CGPA: %.2f | Skills: %s%n",
                        j.getId(), j.getTitle(), j.getCompanyName(), j.getPackageLpa(), j.getMinCgpa(), j.getRequiredSkills());
            }
        }
        promptEnterKey();
    }

    private void handleJobApplication(Student student) {
        TerminalUtils.printSubHeader("APPLY FOR A JOB");
        if (!student.isHasSubmittedCv()) {
            TerminalUtils.printError("You must submit your CV profile (Option 1) before applying for jobs!");
            promptEnterKey();
            return;
        }

        List<JobPosting> jobs = recruitmentService.getAvailableJobsForStudent(student);
        if (jobs.isEmpty()) {
            TerminalUtils.printWarning("There are currently no active job postings available.");
            promptEnterKey();
            return;
        }

        System.out.println("Active Available Jobs:");
        for (JobPosting j : jobs) {
            System.out.printf("  • ID: %s | %s @ %s (Pkg: %.1f LPA, Min CGPA: %.2f)%n",
                    TerminalUtils.CYAN + j.getId() + TerminalUtils.RESET, j.getTitle(), j.getCompanyName(), j.getPackageLpa(), j.getMinCgpa());
        }

        System.out.print("\nEnter Job ID to apply for (e.g. JOB501): ");
        String jobId = scanner.nextLine().trim();

        String result = recruitmentService.applyForJob(student, jobId);
        if (result.startsWith("SUCCESS")) {
            TerminalUtils.printSuccess(result);
        } else {
            TerminalUtils.printError(result);
        }
        promptEnterKey();
    }

    private void trackStudentApplications(Student student) {
        TerminalUtils.printSubHeader("MY APPLICATION TRACKER");
        List<Application> apps = recruitmentService.getStudentApplications(student.getId());
        if (apps.isEmpty()) {
            TerminalUtils.printInfo("You have not applied for any jobs yet.");
        } else {
            System.out.printf("%-10s %-25s %-20s %-18s %-15s%n", "App ID", "Job Title", "Company", "Applied Date", "Status");
            TerminalUtils.printDivider();
            for (Application a : apps) {
                System.out.printf("%-10s %-25s %-20s %-18s %s%n",
                        a.getId(), a.getJobTitle(), a.getCompanyName(), a.getAppliedDate(), TerminalUtils.getStatusBadge(a.getStatus()));
            }
        }
        promptEnterKey();
    }

    // --- Company Portal Dashboard ---

    private void companyDashboard(Company company) {
        boolean inPortal = true;
        while (inPortal) {
            TerminalUtils.printHeader("COMPANY DASHBOARD - " + company.getCompanyName().toUpperCase());
            System.out.println("Company ID: " + TerminalUtils.CYAN + company.getId() + TerminalUtils.RESET +
                    " | Industry: " + company.getIndustry() + " | HQ: " + company.getLocation());
            TerminalUtils.printDivider();
            System.out.println("  1. 📌 Post a New Job Opening");
            System.out.println("  2. 📋 View My Posted Job Openings");
            System.out.println("  3. 👥 View Applicants & Review Student CVs");
            System.out.println("  4. ⚙️  Update Applicant Status (Shortlist / Accept / Reject)");
            System.out.println("  5. ℹ️  View Company Profile Details");
            System.out.println("  0. 🚪 Logout");
            TerminalUtils.printDivider();
            System.out.print(TerminalUtils.CYAN + "Enter option [0-5]: " + TerminalUtils.RESET);

            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    handleCreateJobPosting(company);
                    break;
                case "2":
                    viewCompanyJobs(company);
                    break;
                case "3":
                    viewApplicantsAndCvs(company);
                    break;
                case "4":
                    handleUpdateApplicationStatus(company);
                    break;
                case "5":
                    viewCompanyProfile(company);
                    break;
                case "0":
                    authService.logout();
                    inPortal = false;
                    TerminalUtils.printInfo("Logged out of Company Portal.");
                    break;
                default:
                    TerminalUtils.printError("Invalid option.");
                    promptEnterKey();
                    break;
            }
        }
    }

    private void handleCreateJobPosting(Company company) {
        TerminalUtils.printSubHeader("POST NEW JOB OPENING");
        try {
            System.out.print("Job Role Title (e.g. Software Engineer): ");
            String title = scanner.nextLine().trim();

            System.out.print("Job Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Required Technical Skills: ");
            String skills = scanner.nextLine().trim();

            System.out.print("Annual Package (in LPA / Salary): ");
            double pkg = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Minimum CGPA Eligibility Criterion (e.g., 3.0 or 7.5): ");
            double minCgpa = Double.parseDouble(scanner.nextLine().trim());

            JobPosting job = recruitmentService.createJobPosting(company, title, description, skills, pkg, minCgpa);
            TerminalUtils.printSuccess("Job posting published successfully! Job ID: " + job.getId());
            promptEnterKey();
        } catch (NumberFormatException e) {
            TerminalUtils.printError("Invalid number format for package or CGPA!");
            promptEnterKey();
        } catch (Exception e) {
            TerminalUtils.printError("Failed to create job posting: " + e.getMessage());
            promptEnterKey();
        }
    }

    private void viewCompanyJobs(Company company) {
        TerminalUtils.printSubHeader("MY POSTED JOBS");
        List<JobPosting> jobs = recruitmentService.getCompanyJobPostings(company.getId());
        if (jobs.isEmpty()) {
            TerminalUtils.printInfo("You have not created any job postings yet.");
        } else {
            for (JobPosting j : jobs) {
                List<Application> apps = recruitmentService.getApplicationsForJob(j.getId());
                System.out.printf("[%s] %s | Pkg: %.1f LPA | Min CGPA: %.2f | Applicants: %d%n",
                        TerminalUtils.CYAN + j.getId() + TerminalUtils.RESET,
                        TerminalUtils.BOLD + j.getTitle() + TerminalUtils.RESET,
                        j.getPackageLpa(), j.getMinCgpa(), apps.size());
                System.out.println("     Skills: " + j.getRequiredSkills());
                System.out.println("     Description: " + j.getDescription());
                System.out.println();
            }
        }
        promptEnterKey();
    }

    private void viewApplicantsAndCvs(Company company) {
        TerminalUtils.printSubHeader("APPLICANTS & STUDENT CV REVIEW");
        List<Application> apps = recruitmentService.getApplicationsForCompany(company.getId());
        if (apps.isEmpty()) {
            TerminalUtils.printInfo("No student applications received yet.");
            promptEnterKey();
            return;
        }

        System.out.println("Received Applications:");
        for (Application a : apps) {
            System.out.printf("  • App ID: %s | Student: %s (%s, CGPA: %.2f) | Job: %s | Status: %s%n",
                    TerminalUtils.CYAN + a.getId() + TerminalUtils.RESET,
                    a.getStudentName(), a.getStudentBranch(), a.getStudentCgpa(),
                    a.getJobTitle(), TerminalUtils.getStatusBadge(a.getStatus()));
        }

        System.out.print("\nEnter Student ID or Application ID to inspect full Student CV (or press Enter to return): ");
        String inputId = scanner.nextLine().trim();
        if (inputId.isEmpty()) return;

        Optional<Application> appOpt = recruitmentService.getApplicationsForCompany(company.getId()).stream()
                .filter(a -> a.getId().equalsIgnoreCase(inputId) || a.getStudentId().equalsIgnoreCase(inputId))
                .findFirst();

        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            Optional<Student> studentOpt = recruitmentService.getStudentDetails(app.getStudentId());
            if (studentOpt.isPresent()) {
                Student s = studentOpt.get();
                TerminalUtils.printSubHeader("STUDENT CV & DOSSIER - " + s.getFullName());
                System.out.println("Student ID    : " + s.getId());
                System.out.println("Full Name     : " + s.getFullName());
                System.out.println("Email Contact : " + s.getEmail());
                System.out.println("Phone Contact : " + s.getPhone());
                System.out.println("Department    : " + s.getBranch());
                System.out.println("CGPA          : " + s.getCgpa());
                System.out.println("Key Skills    : " + s.getSkills());
                System.out.println("CV File Path  : " + (s.getCvFilePath().isEmpty() ? "None attached" : s.getCvFilePath()));
                System.out.println("\n--- CV Summary & Background ---");
                System.out.println(s.getCvSummary());
                TerminalUtils.printDivider();
            }
        } else {
            TerminalUtils.printError("No application or student found matching ID: " + inputId);
        }
        promptEnterKey();
    }

    private void handleUpdateApplicationStatus(Company company) {
        TerminalUtils.printSubHeader("UPDATE CANDIDATE APPLICATION STATUS");
        List<Application> apps = recruitmentService.getApplicationsForCompany(company.getId());
        if (apps.isEmpty()) {
            TerminalUtils.printInfo("No student applications available to update.");
            promptEnterKey();
            return;
        }

        System.out.println("Current Applications:");
        for (Application a : apps) {
            System.out.printf("  [%s] Student: %s -> Job: %s | Current Status: %s%n",
                    a.getId(), a.getStudentName(), a.getJobTitle(), TerminalUtils.getStatusBadge(a.getStatus()));
        }

        System.out.print("\nEnter Application ID to update (e.g. APP1001): ");
        String appId = scanner.nextLine().trim();

        Optional<Application> targetAppOpt = apps.stream().filter(a -> a.getId().equalsIgnoreCase(appId)).findFirst();
        if (targetAppOpt.isEmpty()) {
            TerminalUtils.printError("Application ID not found in your company listings!");
            promptEnterKey();
            return;
        }

        System.out.println("\nSelect New Status:");
        System.out.println("  1. SHORTLISTED (Candidate selected for interview)");
        System.out.println("  2. ACCEPTED (Hired / Selected)");
        System.out.println("  3. REJECTED (Not moving forward)");
        System.out.println("  4. PENDING (Under review)");
        System.out.print("Choice [1-4]: ");
        String statusChoice = scanner.nextLine().trim();

        ApplicationStatus newStatus;
        switch (statusChoice) {
            case "1": newStatus = ApplicationStatus.SHORTLISTED; break;
            case "2": newStatus = ApplicationStatus.ACCEPTED; break;
            case "3": newStatus = ApplicationStatus.REJECTED; break;
            case "4": newStatus = ApplicationStatus.PENDING; break;
            default:
                TerminalUtils.printError("Invalid choice! Status unchanged.");
                promptEnterKey();
                return;
        }

        boolean updated = recruitmentService.updateApplicationStatus(appId, newStatus);
        if (updated) {
            TerminalUtils.printSuccess("Application status updated to " + newStatus + "!");
        } else {
            TerminalUtils.printError("Failed to update status.");
        }
        promptEnterKey();
    }

    private void viewCompanyProfile(Company company) {
        TerminalUtils.printSubHeader("COMPANY PROFILE");
        System.out.println("Company Name : " + company.getCompanyName());
        System.out.println("Work Email   : " + company.getEmail());
        System.out.println("Industry     : " + company.getIndustry());
        System.out.println("Location     : " + company.getLocation());
        System.out.println("Website      : " + company.getWebsite());
        System.out.println("Description  : " + company.getDescription());
        promptEnterKey();
    }

    // --- Guest Browsing ---

    private void handleGuestBrowse() {
        displayAllCompaniesAndJobs();
    }

    private void promptEnterKey() {
        System.out.print("\n" + TerminalUtils.YELLOW + "Press ENTER to continue..." + TerminalUtils.RESET);
        scanner.nextLine();
    }
}

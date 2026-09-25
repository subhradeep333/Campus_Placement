package service;

import model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecruitmentService {
    private DataStore dataStore;

    public RecruitmentService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // Student Services
    public boolean updateStudentCv(Student student, String skills, String cvSummary, String cvFilePath) {
        student.setSkills(skills);
        student.setCvSummary(cvSummary);
        student.setCvFilePath(cvFilePath);
        student.setHasSubmittedCv(true);
        PersistenceService.saveDataStore(dataStore);
        return true;
    }

    public List<JobPosting> getAvailableJobsForStudent(Student student) {
        return dataStore.getActiveJobPostings();
    }

    public List<JobPosting> filterJobsBySkillOrTitle(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return dataStore.getActiveJobPostings().stream()
                .filter(j -> j.getTitle().toLowerCase().contains(lowerKeyword) ||
                        j.getRequiredSkills().toLowerCase().contains(lowerKeyword) ||
                        j.getCompanyName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    public String applyForJob(Student student, String jobId) {
        if (!student.isHasSubmittedCv()) {
            return "ERROR: You must submit your CV before applying for jobs!";
        }

        Optional<JobPosting> jobOpt = dataStore.findJobById(jobId);
        if (jobOpt.isEmpty()) {
            return "ERROR: Invalid Job ID!";
        }

        JobPosting job = jobOpt.get();
        if (!job.isActive()) {
            return "ERROR: This job posting is no longer active!";
        }

        if (student.getCgpa() < job.getMinCgpa()) {
            return String.format("ERROR: Your CGPA (%.2f) does not meet the minimum required CGPA (%.2f) for this role.", student.getCgpa(), job.getMinCgpa());
        }

        if (dataStore.findApplication(student.getId(), job.getId()).isPresent()) {
            return "ERROR: You have already applied for this job posting!";
        }

        String appId = dataStore.generateApplicationId();
        Application app = new Application(
                appId,
                job.getId(),
                job.getTitle(),
                student.getId(),
                student.getFullName(),
                student.getBranch(),
                student.getCgpa(),
                job.getCompanyId(),
                job.getCompanyName()
        );

        dataStore.addApplication(app);
        PersistenceService.saveDataStore(dataStore);
        return "SUCCESS: Application submitted successfully! Application ID: " + appId;
    }

    public List<Application> getStudentApplications(String studentId) {
        return dataStore.getApplicationsByStudentId(studentId);
    }

    // Company Services
    public JobPosting createJobPosting(Company company, String title, String description, String requiredSkills, double packageLpa, double minCgpa) {
        String jobId = dataStore.generateJobId();
        JobPosting job = new JobPosting(jobId, company.getId(), company.getCompanyName(), title, description, requiredSkills, packageLpa, minCgpa);
        dataStore.addJobPosting(job);
        PersistenceService.saveDataStore(dataStore);
        return job;
    }

    public List<JobPosting> getCompanyJobPostings(String companyId) {
        return dataStore.getJobPostingsByCompanyId(companyId);
    }

    public List<Application> getApplicationsForCompany(String companyId) {
        return dataStore.getApplicationsByCompanyId(companyId);
    }

    public List<Application> getApplicationsForJob(String jobId) {
        return dataStore.getApplicationsByJobId(jobId);
    }

    public boolean updateApplicationStatus(String applicationId, ApplicationStatus newStatus) {
        Optional<Application> appOpt = dataStore.findApplicationById(applicationId);
        if (appOpt.isPresent()) {
            appOpt.get().setStatus(newStatus);
            PersistenceService.saveDataStore(dataStore);
            return true;
        }
        return false;
    }

    public Optional<Student> getStudentDetails(String studentId) {
        return dataStore.findStudentById(studentId);
    }

    public List<Company> getAllCompanies() {
        return dataStore.getCompanies();
    }

    // Interview Management Services
    public String scheduleInterview(String companyId, String applicationId, String roundType, int roundNumber,
                                    String scheduledDateTime, String locationOrLink, String interviewerName) {
        Optional<Application> appOpt = dataStore.findApplicationById(applicationId);
        if (appOpt.isEmpty()) {
            return "ERROR: Application not found!";
        }

        Application app = appOpt.get();
        if (!app.getCompanyId().equalsIgnoreCase(companyId)) {
            return "ERROR: Unauthorized! This application does not belong to your company.";
        }

        String intId = dataStore.generateInterviewId();
        InterviewRound interview = new InterviewRound(
                intId,
                app.getId(),
                app.getJobId(),
                app.getJobTitle(),
                app.getStudentId(),
                app.getStudentName(),
                app.getCompanyId(),
                app.getCompanyName(),
                roundType,
                roundNumber,
                scheduledDateTime,
                locationOrLink,
                interviewerName
        );

        dataStore.addInterview(interview);

        // Auto-update application status to SHORTLISTED if currently PENDING
        if (app.getStatus() == ApplicationStatus.PENDING) {
            app.setStatus(ApplicationStatus.SHORTLISTED);
        }

        PersistenceService.saveDataStore(dataStore);
        return "SUCCESS: Interview round #" + roundNumber + " (" + roundType + ") scheduled successfully! Interview ID: " + intId;
    }

    public boolean updateInterviewResult(String interviewId, InterviewStatus status, String feedback, double score) {
        Optional<InterviewRound> intOpt = dataStore.findInterviewById(interviewId);
        if (intOpt.isPresent()) {
            InterviewRound ir = intOpt.get();
            ir.setStatus(status);
            if (feedback != null) ir.setFeedback(feedback);
            if (score >= 0) ir.setScore(score);
            PersistenceService.saveDataStore(dataStore);
            return true;
        }
        return false;
    }

    public boolean cancelInterview(String interviewId) {
        Optional<InterviewRound> intOpt = dataStore.findInterviewById(interviewId);
        if (intOpt.isPresent()) {
            intOpt.get().setStatus(InterviewStatus.CANCELLED);
            PersistenceService.saveDataStore(dataStore);
            return true;
        }
        return false;
    }

    public List<InterviewRound> getInterviewsForStudent(String studentId) {
        return dataStore.getInterviewsByStudentId(studentId);
    }

    public List<InterviewRound> getInterviewsForCompany(String companyId) {
        return dataStore.getInterviewsByCompanyId(companyId);
    }

    public List<InterviewRound> getInterviewsForApplication(String applicationId) {
        return dataStore.getInterviewsByApplicationId(applicationId);
    }

    public Optional<InterviewRound> getInterviewDetails(String interviewId) {
        return dataStore.findInterviewById(interviewId);
    }

    // Seed Demo Data
    public void seedDemoData() {
        if (!dataStore.getApplications().isEmpty() && dataStore.getInterviews().isEmpty()) {
            Application a1 = dataStore.getApplications().get(0);
            String int1Id = dataStore.generateInterviewId();
            InterviewRound ir1 = new InterviewRound(
                    int1Id, a1.getId(), a1.getJobId(), a1.getJobTitle(), a1.getStudentId(), a1.getStudentName(), a1.getCompanyId(), a1.getCompanyName(),
                    "Technical Coding Round", 1, "2026-10-05 10:00 AM", "https://meet.google.com/xyz-tech-round", "Dr. Alan Turing"
            );
            ir1.setStatus(InterviewStatus.PASSED);
            ir1.setFeedback("Excellent knowledge of Java concurrency, memory models, and data structure design. Strong problem-solving speed.");
            ir1.setScore(9.2);
            dataStore.addInterview(ir1);

            String int2Id = dataStore.generateInterviewId();
            InterviewRound ir2 = new InterviewRound(
                    int2Id, a1.getId(), a1.getJobId(), a1.getJobTitle(), a1.getStudentId(), a1.getStudentName(), a1.getCompanyId(), a1.getCompanyName(),
                    "System Design & Architecture", 2, "2026-10-12 02:30 PM", "https://meet.google.com/xyz-system-design", "Grace Hopper"
            );
            ir2.setStatus(InterviewStatus.SCHEDULED);
            ir2.setFeedback("Upcoming Round. Candidate instructed to review distributed consensus algorithms.");
            dataStore.addInterview(ir2);

            PersistenceService.saveDataStore(dataStore);
        }

        if (!dataStore.getCompanies().isEmpty() || !dataStore.getStudents().isEmpty()) {
            return; // Data already exists
        }

        // Add 2 Demo Companies
        String c1Id = dataStore.generateCompanyId();
        Company c1 = new Company(c1Id, "recruitment@google.com", "google123", "Google", "Technology / Cloud / AI", "Mountain View, CA", "https://careers.google.com", "Leading global tech company specializing in search, cloud, AI, and consumer tech.");
        dataStore.addCompany(c1);

        String c2Id = dataStore.generateCompanyId();
        Company c2 = new Company(c2Id, "hr@microsoft.com", "ms123", "Microsoft", "Enterprise Software", "Redmond, WA", "https://careers.microsoft.com", "Building platforms and tools to empower every person and organization.");
        dataStore.addCompany(c2);

        String c3Id = dataStore.generateCompanyId();
        Company c3 = new Company(c3Id, "jobs@fintech.com", "fin123", "Apex Financial Technologies", "FinTech / Banking", "New York, NY", "https://apexfintech.com", "High-frequency trading and modern banking software provider.");
        dataStore.addCompany(c3);

        // Add 2 Demo Students
        String s1Id = dataStore.generateStudentId();
        Student s1 = new Student(s1Id, "alex@university.edu", "pass123", "Alex Rivers", "+1-555-0192", "Computer Science", 3.85);
        s1.setSkills("Java, Python, Data Structures, Algorithms, SQL, Git");
        s1.setCvSummary("Senior CS Student with experience in backend Java development, microservices, and algorithm optimization. Built 3 open-source CLI tools.");
        s1.setCvFilePath("/documents/Alex_Rivers_Resume.pdf");
        s1.setHasSubmittedCv(true);
        dataStore.addStudent(s1);

        String s2Id = dataStore.generateStudentId();
        Student s2 = new Student(s2Id, "sarah@university.edu", "pass123", "Sarah Chen", "+1-555-0481", "Software Engineering", 3.92);
        s2.setSkills("Java, Spring Boot, React, Cloud Computing, Docker");
        s2.setCvSummary("Full-stack enthusiast interested in scalable web architecture and cloud-native solutions. Winner of 2025 Hackathon.");
        s2.setCvFilePath("/documents/Sarah_Chen_CV.pdf");
        s2.setHasSubmittedCv(true);
        dataStore.addStudent(s2);

        // Add Demo Job Postings
        String j1Id = dataStore.generateJobId();
        JobPosting j1 = new JobPosting(j1Id, c1Id, c1.getCompanyName(), "Software Engineer I - Backend", "Design and implement scalable REST APIs, microservices, and distributed data pipelines.", "Java, Distributed Systems, SQL", 28.5, 3.5);
        dataStore.addJobPosting(j1);

        String j2Id = dataStore.generateJobId();
        JobPosting j2 = new JobPosting(j2Id, c2Id, c2.getCompanyName(), "Cloud Solutions Developer", "Work with Azure platform engineering teams to build developer tooling.", "Java, C#, Cloud Computing, Kubernetes", 26.0, 3.2);
        dataStore.addJobPosting(j2);

        String j3Id = dataStore.generateJobId();
        JobPosting j3 = new JobPosting(j3Id, c3Id, c3.getCompanyName(), "Quantitative Developer Intern", "Implement high-speed algorithmic trading components in Core Java.", "Java, Concurrency, Algorithms, Low-Latency", 32.0, 3.7);
        dataStore.addJobPosting(j3);

        // Add Demo Applications
        String a1Id = dataStore.generateApplicationId();
        Application a1 = new Application(a1Id, j1Id, j1.getTitle(), s1Id, s1.getFullName(), s1.getBranch(), s1.getCgpa(), c1Id, c1.getCompanyName());
        a1.setStatus(ApplicationStatus.SHORTLISTED);
        dataStore.addApplication(a1);

        String a2Id = dataStore.generateApplicationId();
        Application a2 = new Application(a2Id, j3Id, j3.getTitle(), s2Id, s2.getFullName(), s2.getBranch(), s2.getCgpa(), c3Id, c3.getCompanyName());
        a2.setStatus(ApplicationStatus.PENDING);
        dataStore.addApplication(a2);

        // Add Demo Interviews
        String int1Id = dataStore.generateInterviewId();
        InterviewRound ir1 = new InterviewRound(
                int1Id, a1Id, j1Id, j1.getTitle(), s1Id, s1.getFullName(), c1Id, c1.getCompanyName(),
                "Technical Coding Round", 1, "2026-10-05 10:00 AM", "https://meet.google.com/xyz-tech-round", "Dr. Alan Turing"
        );
        ir1.setStatus(InterviewStatus.PASSED);
        ir1.setFeedback("Excellent knowledge of Java concurrency, memory models, and data structure design. Strong problem-solving speed.");
        ir1.setScore(9.2);
        dataStore.addInterview(ir1);

        String int2Id = dataStore.generateInterviewId();
        InterviewRound ir2 = new InterviewRound(
                int2Id, a1Id, j1Id, j1.getTitle(), s1Id, s1.getFullName(), c1Id, c1.getCompanyName(),
                "System Design & Architecture", 2, "2026-10-12 02:30 PM", "https://meet.google.com/xyz-system-design", "Grace Hopper"
        );
        ir2.setStatus(InterviewStatus.SCHEDULED);
        ir2.setFeedback("Upcoming Round. Candidate instructed to review distributed consensus algorithms.");
        dataStore.addInterview(ir2);

        PersistenceService.saveDataStore(dataStore);
    }
}

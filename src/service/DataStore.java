package service;

import model.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Student> students;
    private List<Company> companies;
    private List<JobPosting> jobPostings;
    private List<Application> applications;

    private AtomicInteger studentCounter;
    private AtomicInteger companyCounter;
    private AtomicInteger jobCounter;
    private AtomicInteger applicationCounter;

    public DataStore() {
        this.students = new ArrayList<>();
        this.companies = new ArrayList<>();
        this.jobPostings = new ArrayList<>();
        this.applications = new ArrayList<>();

        this.studentCounter = new AtomicInteger(100);
        this.companyCounter = new AtomicInteger(200);
        this.jobCounter = new AtomicInteger(500);
        this.applicationCounter = new AtomicInteger(1000);
    }

    public String generateStudentId() {
        return "STU" + studentCounter.incrementAndGet();
    }

    public String generateCompanyId() {
        return "CMP" + companyCounter.incrementAndGet();
    }

    public String generateJobId() {
        return "JOB" + jobCounter.incrementAndGet();
    }

    public String generateApplicationId() {
        return "APP" + applicationCounter.incrementAndGet();
    }

    // Students
    public void addStudent(Student student) {
        students.add(student);
        if (student != null && student.getId() != null && student.getId().startsWith("STU")) {
            try {
                int num = Integer.parseInt(student.getId().substring(3));
                studentCounter.updateAndGet(curr -> Math.max(curr, num));
            } catch (NumberFormatException ignored) {}
        }
    }

    public List<Student> getStudents() {
        return students;
    }

    public Optional<Student> findStudentByEmail(String email) {
        return students.stream()
                .filter(s -> s.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    public Optional<Student> findStudentById(String id) {
        return students.stream()
                .filter(s -> s.getId().equalsIgnoreCase(id.trim()))
                .findFirst();
    }

    // Companies
    public void addCompany(Company company) {
        companies.add(company);
        if (company != null && company.getId() != null && company.getId().startsWith("CMP")) {
            try {
                int num = Integer.parseInt(company.getId().substring(3));
                companyCounter.updateAndGet(curr -> Math.max(curr, num));
            } catch (NumberFormatException ignored) {}
        }
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public Optional<Company> findCompanyByEmail(String email) {
        return companies.stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    public Optional<Company> findCompanyById(String id) {
        return companies.stream()
                .filter(c -> c.getId().equalsIgnoreCase(id.trim()))
                .findFirst();
    }

    // Job Postings
    public void addJobPosting(JobPosting job) {
        jobPostings.add(job);
        if (job != null && job.getId() != null && job.getId().startsWith("JOB")) {
            try {
                int num = Integer.parseInt(job.getId().substring(3));
                jobCounter.updateAndGet(curr -> Math.max(curr, num));
            } catch (NumberFormatException ignored) {}
        }
    }

    public List<JobPosting> getJobPostings() {
        return jobPostings;
    }

    public List<JobPosting> getActiveJobPostings() {
        List<JobPosting> activeJobs = new ArrayList<>();
        for (JobPosting j : jobPostings) {
            if (j.isActive()) {
                activeJobs.add(j);
            }
        }
        return activeJobs;
    }

    public List<JobPosting> getJobPostingsByCompanyId(String companyId) {
        List<JobPosting> companyJobs = new ArrayList<>();
        for (JobPosting j : jobPostings) {
            if (j.getCompanyId().equalsIgnoreCase(companyId)) {
                companyJobs.add(j);
            }
        }
        return companyJobs;
    }

    public Optional<JobPosting> findJobById(String jobId) {
        return jobPostings.stream()
                .filter(j -> j.getId().equalsIgnoreCase(jobId.trim()))
                .findFirst();
    }

    // Applications
    public void addApplication(Application app) {
        applications.add(app);
        if (app != null && app.getId() != null && app.getId().startsWith("APP")) {
            try {
                int num = Integer.parseInt(app.getId().substring(3));
                applicationCounter.updateAndGet(curr -> Math.max(curr, num));
            } catch (NumberFormatException ignored) {}
        }
    }

    public List<Application> getApplications() {
        return applications;
    }

    public List<Application> getApplicationsByStudentId(String studentId) {
        List<Application> studentApps = new ArrayList<>();
        for (Application a : applications) {
            if (a.getStudentId().equalsIgnoreCase(studentId)) {
                studentApps.add(a);
            }
        }
        return studentApps;
    }

    public List<Application> getApplicationsByJobId(String jobId) {
        List<Application> jobApps = new ArrayList<>();
        for (Application a : applications) {
            if (a.getJobId().equalsIgnoreCase(jobId)) {
                jobApps.add(a);
            }
        }
        return jobApps;
    }

    public List<Application> getApplicationsByCompanyId(String companyId) {
        List<Application> companyApps = new ArrayList<>();
        for (Application a : applications) {
            if (a.getCompanyId().equalsIgnoreCase(companyId)) {
                companyApps.add(a);
            }
        }
        return companyApps;
    }

    public Optional<Application> findApplication(String studentId, String jobId) {
        return applications.stream()
                .filter(a -> a.getStudentId().equalsIgnoreCase(studentId) && a.getJobId().equalsIgnoreCase(jobId))
                .findFirst();
    }

    public Optional<Application> findApplicationById(String appId) {
        return applications.stream()
                .filter(a -> a.getId().equalsIgnoreCase(appId.trim()))
                .findFirst();
    }
}

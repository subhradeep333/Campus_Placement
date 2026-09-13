package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Application implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String jobId;
    private String jobTitle;
    private String studentId;
    private String studentName;
    private String studentBranch;
    private double studentCgpa;
    private String companyId;
    private String companyName;
    private ApplicationStatus status;
    private String appliedDate;

    public Application(String id, String jobId, String jobTitle, String studentId, String studentName, String studentBranch, double studentCgpa, String companyId, String companyName) {
        this.id = id;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentBranch = studentBranch;
        this.studentCgpa = studentCgpa;
        this.companyId = companyId;
        this.companyName = companyName;
        this.status = ApplicationStatus.PENDING;
        this.appliedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentBranch() {
        return studentBranch;
    }

    public void setStudentBranch(String studentBranch) {
        this.studentBranch = studentBranch;
    }

    public double getStudentCgpa() {
        return studentCgpa;
    }

    public void setStudentCgpa(double studentCgpa) {
        this.studentCgpa = studentCgpa;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }
}

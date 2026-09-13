package model;

public class Student extends User {
    private static final long serialVersionUID = 1L;

    private String fullName;
    private String phone;
    private String branch;
    private double cgpa;
    private String skills;
    private String cvSummary;
    private String cvFilePath;
    private boolean hasSubmittedCv;

    public Student(String id, String email, String password, String fullName, String phone, String branch, double cgpa) {
        super(id, email, password, UserRole.STUDENT);
        this.fullName = fullName;
        this.phone = phone;
        this.branch = branch;
        this.cgpa = cgpa;
        this.skills = "Not specified";
        this.cvSummary = "";
        this.cvFilePath = "";
        this.hasSubmittedCv = false;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getCvSummary() {
        return cvSummary;
    }

    public void setCvSummary(String cvSummary) {
        this.cvSummary = cvSummary;
    }

    public String getCvFilePath() {
        return cvFilePath;
    }

    public void setCvFilePath(String cvFilePath) {
        this.cvFilePath = cvFilePath;
    }

    public boolean isHasSubmittedCv() {
        return hasSubmittedCv;
    }

    public void setHasSubmittedCv(boolean hasSubmittedCv) {
        this.hasSubmittedCv = hasSubmittedCv;
    }
}

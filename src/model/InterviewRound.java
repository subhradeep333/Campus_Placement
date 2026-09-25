package model;

import java.io.Serializable;

public class InterviewRound implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String applicationId;
    private String jobId;
    private String jobTitle;
    private String studentId;
    private String studentName;
    private String companyId;
    private String companyName;
    private String roundType;
    private int roundNumber;
    private String scheduledDateTime;
    private String locationOrLink;
    private String interviewerName;
    private InterviewStatus status;
    private String feedback;
    private double score;

    public InterviewRound(String id, String applicationId, String jobId, String jobTitle,
                          String studentId, String studentName, String companyId, String companyName,
                          String roundType, int roundNumber, String scheduledDateTime,
                          String locationOrLink, String interviewerName) {
        this.id = id;
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.companyId = companyId;
        this.companyName = companyName;
        this.roundType = roundType;
        this.roundNumber = roundNumber;
        this.scheduledDateTime = scheduledDateTime;
        this.locationOrLink = locationOrLink;
        this.interviewerName = interviewerName;
        this.status = InterviewStatus.SCHEDULED;
        this.feedback = "";
        this.score = 0.0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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

    public String getRoundType() {
        return roundType;
    }

    public void setRoundType(String roundType) {
        this.roundType = roundType;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getScheduledDateTime() {
        return scheduledDateTime;
    }

    public void setScheduledDateTime(String scheduledDateTime) {
        this.scheduledDateTime = scheduledDateTime;
    }

    public String getLocationOrLink() {
        return locationOrLink;
    }

    public void setLocationOrLink(String locationOrLink) {
        this.locationOrLink = locationOrLink;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public InterviewStatus getStatus() {
        return status;
    }

    public void setStatus(InterviewStatus status) {
        this.status = status;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}

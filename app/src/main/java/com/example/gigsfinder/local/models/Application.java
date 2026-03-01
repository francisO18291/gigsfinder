package com.example.gigsfinder.local.models;
public class Application {
    private String applicationId;
    private String jobId;
    private String jobTitle;
    private String seekerId;
    private String seekerName;
    private String seekerEmail;
    private String seekerPhone;
    private String coverLetter;
    private String resumeUrl;
    private String status;
    private long appliedAt;
    private long respondedAt;

    public Application() {

        this.status = "pending";
    }

    public Application(String jobId, String jobTitle, String seekerId, String seekerName) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.seekerId = seekerId;
        this.seekerName = seekerName;
        this.appliedAt = System.currentTimeMillis();
        this.status = "pending";
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

    public String getSeekerId() {
        return seekerId;
    }

    public void setSeekerId(String seekerId) {
        this.seekerId = seekerId;
    }

    public String getSeekerName() {
        return seekerName;
    }

    public void setSeekerName(String seekerName) {
        this.seekerName = seekerName;
    }

    public String getSeekerEmail() {
        return seekerEmail;
    }

    public void setSeekerEmail(String seekerEmail) {
        this.seekerEmail = seekerEmail;
    }

    public String getSeekerPhone() {
        return seekerPhone;
    }

    public void setSeekerPhone(String seekerPhone) {
        this.seekerPhone = seekerPhone;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(long appliedAt) {
        this.appliedAt = appliedAt;
    }

    public long getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(long respondedAt) {
        this.respondedAt = respondedAt;
    }
}
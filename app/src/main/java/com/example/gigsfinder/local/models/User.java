package com.example.gigsfinder.local.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String email;
    private String name;
    private String phone;
    private String userType;
    private String location;
    private String resumeUrl;
    private String profileImageUrl;
    private List<String> skills;
    private double rating;
    private int totalRatings;
    private long createdAt;

    public User() {

        this.skills = new ArrayList<>();
        this.rating = 0.0;
        this.totalRatings = 0;
    }

    public User(String userId, String email, String name, String userType) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.userType = userType;
        this.skills = new ArrayList<>();
        this.rating = 0.0;
        this.totalRatings = 0;
        this.createdAt = System.currentTimeMillis();
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
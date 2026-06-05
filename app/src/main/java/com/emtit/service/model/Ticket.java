package com.emtit.service.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Ticket {
    private static int counter = 1000;
    private int id;
    private String title;
    private String description;
    private String reporterName;
    private String reporterPhone;
    private String company;
    private String category;
    private int priority;
    private String createdAt;

    public Ticket() {
        this.id = ++counter;
        this.createdAt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getReporterPhone() { return reporterPhone; }
    public void setReporterPhone(String reporterPhone) { this.reporterPhone = reporterPhone; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getCreatedAt() { return createdAt; }

    public String getPriorityLabel() {
        switch (priority) {
            case 0: return "גבוהה";
            case 1: return "בינונית";
            case 2: return "נמוכה";
            default: return "בינונית";
        }
    }

    public String getPriorityHex() {
        switch (priority) {
            case 0: return "#FF4444";
            case 1: return "#FFA500";
            case 2: return "#00C9A7";
            default: return "#FFA500";
        }
    }
}

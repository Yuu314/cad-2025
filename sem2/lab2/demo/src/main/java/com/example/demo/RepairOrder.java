package com.example.demo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RepairOrder {
    private static Long nextId = 1L;
    private Long id;
    private String clientName;
    private String carModel;
    private String licensePlate;
    private String description;
    private boolean completed;
    private LocalDateTime deadline;
    private String assignedMechanic;
    private double cost;
    private Category category; // НОВОЕ ПОЛЕ
    private List<Comment> comments = new ArrayList<>(); // НОВОЕ ПОЛЕ

    // Конструкторы остаются без изменений
    public RepairOrder() {
        this.id = generateId();
    }

    public RepairOrder(String clientName, String carModel, String licensePlate, String description) {
        this.id = generateId();
        this.clientName = clientName;
        this.carModel = carModel;
        this.licensePlate = licensePlate;
        this.description = description;
        this.completed = false;
        this.cost = 0.0;
    }

    private synchronized Long generateId() {
        return nextId++;
    }

    // Добавьте новые геттеры и сеттеры
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    // Остальные геттеры/сеттеры без изменений...
    public Long getId() { return id; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public String getAssignedMechanic() { return assignedMechanic; }
    public void setAssignedMechanic(String assignedMechanic) { this.assignedMechanic = assignedMechanic; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public boolean isOverdue() {
        if (deadline == null || completed) {
            return false;
        }
        return LocalDateTime.now().isAfter(deadline);
    }

    @Override
    public String toString() {
        return "RepairOrder{" +
                "id=" + id +
                ", clientName='" + clientName + '\'' +
                ", carModel='" + carModel + '\'' +
                ", licensePlate='" + licensePlate + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                ", deadline=" + deadline +
                ", assignedMechanic='" + assignedMechanic + '\'' +
                ", cost=" + cost +
                ", category=" + (category != null ? category.getName() : "null") +
                ", comments=" + comments.size() +
                ", overdue=" + isOverdue() +
                '}';
    }
}
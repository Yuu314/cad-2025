package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false, length = 200)
    private String serviceName;

    @Column(name = "cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "execution_hours", nullable = false)
    private Integer executionHours = 0;

    @Column(name = "execution_minutes", nullable = false)
    private Integer executionMinutes = 0;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Конструкторы
    public Service() {
    }

    public Service(String serviceName, BigDecimal cost, Integer executionHours, Integer executionMinutes, String description) {
        this.serviceName = serviceName;
        this.cost = cost;
        this.executionHours = executionHours;
        this.executionMinutes = executionMinutes;
        this.description = description;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Integer getExecutionHours() {
        return executionHours;
    }

    public void setExecutionHours(Integer executionHours) {
        this.executionHours = executionHours;
    }

    public Integer getExecutionMinutes() {
        return executionMinutes;
    }

    public void setExecutionMinutes(Integer executionMinutes) {
        this.executionMinutes = executionMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Вспомогательный метод для получения времени выполнения
    public String getExecutionTime() {
        if (executionHours == 0 && executionMinutes == 0) {
            return "Не указано";
        } else if (executionHours == 0) {
            return executionMinutes + " мин.";
        } else if (executionMinutes == 0) {
            return executionHours + " ч.";
        } else {
            return executionHours + " ч. " + executionMinutes + " мин.";
        }
    }

    // Вспомогательный метод для получения полной информации
    public String getFullInfo() {
        return serviceName + " - " + cost + " руб. (" + getExecutionTime() + ")";
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                ", cost=" + cost +
                ", executionHours=" + executionHours +
                ", executionMinutes=" + executionMinutes +
                ", description='" + description + '\'' +
                '}';
    }
}
package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.Year;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "license_plate", nullable = false, unique = true, length = 15)
    private String licensePlate;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 70)
    private String model;

    @Column(name = "vin", unique = true, length = 17)
    private String vin;

    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture;

    // Конструкторы
    public Car() {
    }

    public Car(Client client, String licensePlate, String brand, String model, String vin, Integer yearOfManufacture) {
        this.client = client;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.vin = vin;
        this.yearOfManufacture = yearOfManufacture;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Integer getYearOfManufacture() {
        return yearOfManufacture;
    }

    public void setYearOfManufacture(Integer yearOfManufacture) {
        this.yearOfManufacture = yearOfManufacture;
    }

    // Вспомогательный метод для получения полного названия автомобиля
    public String getFullName() {
        return brand + " " + model + " (" + licensePlate + ")";
    }

    // Вспомогательный метод для получения информации о годе
    public String getYearInfo() {
        if (yearOfManufacture != null) {
            return yearOfManufacture.toString() + " г.в.";
        }
        return "Год не указан";
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", client=" + (client != null ? client.getLastName() + " " + client.getFirstName() : "null") +
                ", licensePlate='" + licensePlate + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", vin='" + vin + '\'' +
                ", yearOfManufacture=" + yearOfManufacture +
                '}';
    }
}
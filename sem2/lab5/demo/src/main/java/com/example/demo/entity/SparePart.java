package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "spareparts")
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_name", nullable = false)
    private String partName;

    @Column(name = "part_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal partCost;

    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock = 0;

    // Конструкторы
    public SparePart() {
    }

    public SparePart(String partName, BigDecimal partCost, Integer quantityInStock) {
        this.partName = partName;
        this.partCost = partCost;
        this.quantityInStock = quantityInStock;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public BigDecimal getPartCost() {
        return partCost;
    }

    public void setPartCost(BigDecimal partCost) {
        this.partCost = partCost;
    }

    public Integer getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(Integer quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    // Вспомогательные методы
    public void increaseStock(int quantity) {
        this.quantityInStock += quantity;
    }

    public void decreaseStock(int quantity) {
        if (this.quantityInStock >= quantity) {
            this.quantityInStock -= quantity;
        }
    }

    @Override
    public String toString() {
        return "SparePart{" +
                "id=" + id +
                ", partName='" + partName + '\'' +
                ", partCost=" + partCost +
                ", quantityInStock=" + quantityInStock +
                '}';
    }
}
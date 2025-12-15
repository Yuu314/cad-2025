package com.example.demo.repository;

import com.example.demo.entity.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    // Поиск по названию (без учета регистра, частичное совпадение)
    @Query("SELECT s FROM SparePart s WHERE LOWER(s.partName) LIKE LOWER(CONCAT('%', :partName, '%'))")
    List<SparePart> findByPartNameContainingIgnoreCase(@Param("partName") String partName);

    // Поиск по цене (меньше или равно)
    List<SparePart> findByPartCostLessThanEqual(BigDecimal maxCost);

    // Поиск по наличию на складе (больше 0)
    List<SparePart> findByQuantityInStockGreaterThan(Integer minQuantity);

    // Поиск по названию и наличию
    @Query("SELECT s FROM SparePart s WHERE LOWER(s.partName) LIKE LOWER(CONCAT('%', :partName, '%')) " +
            "AND s.quantityInStock > :minQuantity")
    List<SparePart> findByPartNameContainingAndQuantityInStockGreaterThan(
            @Param("partName") String partName,
            @Param("minQuantity") Integer minQuantity);
}
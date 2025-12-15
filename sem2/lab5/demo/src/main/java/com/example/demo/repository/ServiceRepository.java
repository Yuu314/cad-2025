package com.example.demo.repository;

import com.example.demo.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    // Поиск по названию услуги (без учета регистра, частичное совпадение)
    @Query("SELECT s FROM Service s WHERE LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :serviceName, '%'))")
    List<Service> findByServiceNameContainingIgnoreCase(@Param("serviceName") String serviceName);

    // Поиск по описанию (без учета регистра, частичное совпадение)
    @Query("SELECT s FROM Service s WHERE LOWER(s.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Service> findByDescriptionContainingIgnoreCase(@Param("description") String description);

    // Поиск услуг по цене (до указанной цены)
    @Query("SELECT s FROM Service s WHERE s.cost <= :maxCost")
    List<Service> findByCostLessThanEqual(@Param("maxCost") BigDecimal maxCost);

    // Поиск услуг по цене (от указанной цены)
    @Query("SELECT s FROM Service s WHERE s.cost >= :minCost")
    List<Service> findByCostGreaterThanEqual(@Param("minCost") BigDecimal minCost);

    // Поиск услуг в диапазоне цен
    @Query("SELECT s FROM Service s WHERE s.cost BETWEEN :minCost AND :maxCost")
    List<Service> findByCostBetween(@Param("minCost") BigDecimal minCost, @Param("maxCost") BigDecimal maxCost);

    // Поиск по времени выполнения (часы)
    @Query("SELECT s FROM Service s WHERE s.executionHours = :hours")
    List<Service> findByExecutionHours(@Param("hours") Integer hours);

    // Поиск по времени выполнения (минуты)
    @Query("SELECT s FROM Service s WHERE s.executionMinutes = :minutes")
    List<Service> findByExecutionMinutes(@Param("minutes") Integer minutes);
}
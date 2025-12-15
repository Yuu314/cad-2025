package com.example.demo.repository;

import com.example.demo.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    // Поиск по номеру автомобиля (частичное совпадение)
    @Query("SELECT c FROM Car c WHERE c.licensePlate LIKE CONCAT('%', :licensePlate, '%')")
    List<Car> findByLicensePlateContaining(@Param("licensePlate") String licensePlate);

    // Поиск по марке (без учета регистра, частичное совпадение)
    @Query("SELECT c FROM Car c WHERE LOWER(c.brand) LIKE LOWER(CONCAT('%', :brand, '%'))")
    List<Car> findByBrandContainingIgnoreCase(@Param("brand") String brand);

    // Поиск по модели (без учета регистра, частичное совпадение)
    @Query("SELECT c FROM Car c WHERE LOWER(c.model) LIKE LOWER(CONCAT('%', :model, '%'))")
    List<Car> findByModelContainingIgnoreCase(@Param("model") String model);

    // Поиск по VIN (частичное совпадение)
    @Query("SELECT c FROM Car c WHERE c.vin LIKE CONCAT('%', :vin, '%')")
    List<Car> findByVinContaining(@Param("vin") String vin);

    // Поиск по клиенту
    @Query("SELECT c FROM Car c WHERE c.client.id = :clientId")
    List<Car> findByClientId(@Param("clientId") Long clientId);

    // Поиск по ФИО клиента
    @Query("SELECT c FROM Car c WHERE LOWER(CONCAT(c.client.lastName, ' ', c.client.firstName)) " +
            "LIKE LOWER(CONCAT('%', :clientName, '%'))")
    List<Car> findByClientNameContainingIgnoreCase(@Param("clientName") String clientName);

    // Проверка существования автомобиля по номеру
    boolean existsByLicensePlate(String licensePlate);

    // Проверка существования автомобиля по VIN
    boolean existsByVin(String vin);

    // Получение автомобилей по ID клиента
    List<Car> findByClient_Id(Long clientId);
}
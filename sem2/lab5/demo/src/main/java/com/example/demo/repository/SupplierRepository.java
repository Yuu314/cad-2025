package com.example.demo.repository;

import com.example.demo.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Поиск по названию поставщика (без учета регистра, частичное совпадение)
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%'))")
    List<Supplier> findBySupplierNameContainingIgnoreCase(@Param("supplierName") String supplierName);

    // Поиск по контактному лицу (без учета регистра, частичное совпадение)
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :contactPerson, '%'))")
    List<Supplier> findByContactPersonContainingIgnoreCase(@Param("contactPerson") String contactPerson);

    // Поиск по телефону (частичное совпадение)
    @Query("SELECT s FROM Supplier s WHERE s.phone LIKE CONCAT('%', :phone, '%')")
    List<Supplier> findByPhoneContaining(@Param("phone") String phone);

    // Поиск по email (без учета регистра, частичное совпадение)
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<Supplier> findByEmailContainingIgnoreCase(@Param("email") String email);

    // Проверка существования поставщика по названию
    boolean existsBySupplierName(String supplierName);
}
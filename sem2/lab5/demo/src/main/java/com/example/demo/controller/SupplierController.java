package com.example.demo.controller;

import com.example.demo.entity.Supplier;
import com.example.demo.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    @Autowired
    private SupplierRepository supplierRepository;

    // ========== ОТОБРАЖЕНИЕ ПОСТАВЩИКОВ И ПОИСК ==========
    @GetMapping
    public String listSuppliers(@RequestParam(name = "searchType", required = false) String searchType,
                                @RequestParam(name = "searchValue", required = false) String searchValue,
                                Model model) {

        List<Supplier> suppliers;
        String searchMessage = "";

        try {
            if (searchValue != null && !searchValue.trim().isEmpty()) {
                // Ручная фильтрация (поиск)
                String trimmedValue = searchValue.trim().toLowerCase();
                List<Supplier> allSuppliers = supplierRepository.findAll();

                suppliers = allSuppliers.stream()
                        .filter(supplier -> {
                            String type = (searchType != null) ? searchType : "supplierName";
                            switch (type) {
                                case "supplierName":
                                    return supplier.getSupplierName() != null &&
                                            supplier.getSupplierName().toLowerCase().contains(trimmedValue);
                                case "contactPerson":
                                    return supplier.getContactPerson() != null &&
                                            supplier.getContactPerson().toLowerCase().contains(trimmedValue);
                                case "phone":
                                    return supplier.getPhone() != null &&
                                            supplier.getPhone().toLowerCase().contains(trimmedValue);
                                case "email":
                                    return supplier.getEmail() != null &&
                                            supplier.getEmail().toLowerCase().contains(trimmedValue);
                                default:
                                    return true;
                            }
                        })
                        .collect(Collectors.toList());

                searchMessage = "Результаты поиска";
                logger.info("Search by {}: '{}', found {} suppliers", searchType, trimmedValue, suppliers.size());

            } else {
                // Все поставщики
                suppliers = supplierRepository.findAll();
                searchMessage = "Все поставщики";
            }

        } catch (Exception e) {
            logger.error("Error loading suppliers: {}", e.getMessage(), e);
            suppliers = supplierRepository.findAll();
            model.addAttribute("errorMessage", "Ошибка при загрузке поставщиков");
            searchMessage = "Ошибка при выполнении поиска";
        }

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("searchType", searchType != null ? searchType : "supplierName");
        model.addAttribute("searchValue", searchValue != null ? searchValue : "");
        model.addAttribute("searchMessage", searchMessage);
        model.addAttribute("totalSuppliers", suppliers.size());

        return "html/suppliers";  // Шаблон будет создан отдельно
    }

    // ========== ДОБАВЛЕНИЕ ПОСТАВЩИКА ==========
    @PostMapping("/add")
    public String addSupplier(@ModelAttribute Supplier supplier,
                              RedirectAttributes redirectAttributes) {
        try {
            logger.info("Adding supplier: {} (Contact: {}, Phone: {})",
                    supplier.getSupplierName(), supplier.getContactPerson(), supplier.getPhone());

            // Проверка уникальности названия поставщика
            if (supplierRepository.existsBySupplierName(supplier.getSupplierName())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка: поставщик с названием '" + supplier.getSupplierName() + "' уже существует!");
                return "redirect:/suppliers";
            }

            supplierRepository.save(supplier);
            logger.info("Supplier added with ID: {}", supplier.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Поставщик " + supplier.getSupplierName() + " успешно добавлен!");

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: название поставщика '" + supplier.getSupplierName() + "' уже существует!");
        } catch (Exception e) {
            logger.error("Error adding supplier: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении поставщика");
        }

        return "redirect:/suppliers";
    }

    // ========== УДАЛЕНИЕ ПОСТАВЩИКА ==========
    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable("id") Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            logger.info("Deleting supplier with id: {}", id);

            Supplier supplier = supplierRepository.findById(id).orElse(null);

            if (supplier != null) {
                String supplierName = supplier.getSupplierName();
                supplierRepository.delete(supplier);
                logger.info("Supplier {} deleted", supplierName);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Поставщик " + supplierName + " успешно удалён!");
            } else {
                logger.warn("Supplier with id {} not found", id);
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Поставщик с ID " + id + " не найден!");
            }

        } catch (DataIntegrityViolationException e) {
            logger.error("Cannot delete supplier (foreign key constraint): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Невозможно удалить поставщика: он связан с заказами или запчастями!");
        } catch (Exception e) {
            logger.error("Error deleting supplier: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении поставщика");
        }

        return "redirect:/suppliers";
    }

    // ========== ФОРМА РЕДАКТИРОВАНИЯ ==========
    @GetMapping("/edit/{id}")
    public String editSupplierForm(@PathVariable("id") Long id, Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            logger.info("Loading edit form for supplier id: {}", id);

            Supplier supplier = supplierRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Поставщик не найден"));

            model.addAttribute("supplier", supplier);
            return "html/edit-supplier";  // Шаблон будет создан отдельно

        } catch (Exception e) {
            logger.error("Error loading edit form: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при загрузке формы: " + e.getMessage());
            return "redirect:/suppliers";
        }
    }

    // ========== ОБНОВЛЕНИЕ ПОСТАВЩИКА ==========
    @PostMapping("/update/{id}")
    public String updateSupplier(@PathVariable("id") Long id,
                                 @ModelAttribute Supplier supplier,
                                 RedirectAttributes redirectAttributes) {
        try {
            logger.info("Updating supplier with id: {}", id);

            // Проверяем существование поставщика
            Supplier existingSupplier = supplierRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Поставщик не найден"));

            // Проверяем уникальность названия (если изменилось)
            if (!existingSupplier.getSupplierName().equals(supplier.getSupplierName()) &&
                    supplierRepository.existsBySupplierName(supplier.getSupplierName())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка: поставщик с названием '" + supplier.getSupplierName() + "' уже существует!");
                return "redirect:/suppliers/edit/" + id;
            }

            // Устанавливаем ID
            supplier.setId(id);

            // Сохраняем
            supplierRepository.save(supplier);
            logger.info("Supplier updated successfully");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Данные поставщика успешно обновлены!");

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity error on update: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: название поставщика '" + supplier.getSupplierName() + "' уже существует!");
        } catch (Exception e) {
            logger.error("Error updating supplier: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении поставщика");
        }

        return "redirect:/suppliers";
    }
}
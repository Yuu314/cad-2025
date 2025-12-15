package com.example.demo.controller;

import com.example.demo.entity.Service;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/services")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private ServiceRepository serviceRepository;

    // ========== ОТОБРАЖЕНИЕ УСЛУГ И ПОИСК ==========
    @GetMapping
    public String listServices(@RequestParam(name = "searchType", required = false) String searchType,
                               @RequestParam(name = "searchValue", required = false) String searchValue,
                               Model model) {

        List<Service> services;
        String searchMessage = "";

        try {
            if (searchValue != null && !searchValue.trim().isEmpty()) {
                // Ручная фильтрация (поиск)
                String trimmedValue = searchValue.trim().toLowerCase();
                List<Service> allServices = serviceRepository.findAll();

                services = allServices.stream()
                        .filter(service -> {
                            String type = (searchType != null) ? searchType : "serviceName";
                            switch (type) {
                                case "serviceName":
                                    return service.getServiceName() != null &&
                                            service.getServiceName().toLowerCase().contains(trimmedValue);
                                case "description":
                                    return service.getDescription() != null &&
                                            service.getDescription().toLowerCase().contains(trimmedValue);
                                case "cost":
                                    try {
                                        BigDecimal maxCost = new BigDecimal(trimmedValue);
                                        return service.getCost().compareTo(maxCost) <= 0;
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                default:
                                    return true;
                            }
                        })
                        .collect(Collectors.toList());

                searchMessage = "Результаты поиска";
                logger.info("Search by {}: '{}', found {} services", searchType, trimmedValue, services.size());

            } else {
                // Все услуги
                services = serviceRepository.findAll();
                searchMessage = "Все услуги";
            }

        } catch (Exception e) {
            logger.error("Error loading services: {}", e.getMessage(), e);
            services = serviceRepository.findAll();
            model.addAttribute("errorMessage", "Ошибка при загрузке услуг");
            searchMessage = "Ошибка при выполнении поиска";
        }

        model.addAttribute("services", services);
        model.addAttribute("searchType", searchType != null ? searchType : "serviceName");
        model.addAttribute("searchValue", searchValue != null ? searchValue : "");
        model.addAttribute("searchMessage", searchMessage);
        model.addAttribute("totalServices", services.size());

        return "html/services";
    }

    // ========== ДОБАВЛЕНИЕ УСЛУГИ ==========
    @PostMapping("/add")
    public String addService(@RequestParam("serviceName") String serviceName,
                             @RequestParam("cost") BigDecimal cost,
                             @RequestParam(value = "executionHours", required = false, defaultValue = "0") Integer executionHours,
                             @RequestParam(value = "executionMinutes", required = false, defaultValue = "0") Integer executionMinutes,
                             @RequestParam(value = "description", required = false) String description,
                             RedirectAttributes redirectAttributes) {
        try {
            logger.info("Adding service: {} (Cost: {})", serviceName, cost);

            // Валидация времени
            if (executionHours == null) executionHours = 0;
            if (executionMinutes == null) executionMinutes = 0;

            if (executionHours < 0 || executionMinutes < 0 || executionMinutes >= 60) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка: некорректное время выполнения!");
                return "redirect:/services";
            }

            // Создаем услугу
            Service service = new Service();
            service.setServiceName(serviceName);
            service.setCost(cost);
            service.setExecutionHours(executionHours);
            service.setExecutionMinutes(executionMinutes);
            service.setDescription(description);

            serviceRepository.save(service);
            logger.info("Service added with ID: {}", service.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Услуга '" + serviceName + "' успешно добавлена!");

        } catch (Exception e) {
            logger.error("Error adding service: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении услуги");
        }

        return "redirect:/services";
    }

    // ========== УДАЛЕНИЕ УСЛУГИ ==========
    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable("id") Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Deleting service with id: {}", id);

            Service service = serviceRepository.findById(id).orElse(null);

            if (service != null) {
                String serviceName = service.getServiceName();
                serviceRepository.delete(service);
                logger.info("Service {} deleted", serviceName);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Услуга '" + serviceName + "' успешно удалена!");
            } else {
                logger.warn("Service with id {} not found", id);
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Услуга с ID " + id + " не найдена!");
            }

        } catch (Exception e) {
            logger.error("Error deleting service: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении услуги");
        }

        return "redirect:/services";
    }

    // ========== ФОРМА РЕДАКТИРОВАНИЯ ==========
    @GetMapping("/edit/{id}")
    public String editServiceForm(@PathVariable("id") Long id, Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            logger.info("Loading edit form for service id: {}", id);

            Service service = serviceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Услуга не найдена"));

            model.addAttribute("service", service);
            return "html/edit-service";

        } catch (Exception e) {
            logger.error("Error loading edit form: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при загрузке формы: " + e.getMessage());
            return "redirect:/services";
        }
    }

    // ========== ОБНОВЛЕНИЕ УСЛУГИ ==========
    @PostMapping("/update/{id}")
    public String updateService(@PathVariable("id") Long id,
                                @RequestParam("serviceName") String serviceName,
                                @RequestParam("cost") BigDecimal cost,
                                @RequestParam(value = "executionHours", required = false, defaultValue = "0") Integer executionHours,
                                @RequestParam(value = "executionMinutes", required = false, defaultValue = "0") Integer executionMinutes,
                                @RequestParam(value = "description", required = false) String description,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Updating service with id: {}", id);

            // Проверяем существование услуги
            Service existingService = serviceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Услуга не найдена"));

            // Валидация времени
            if (executionHours == null) executionHours = 0;
            if (executionMinutes == null) executionMinutes = 0;

            if (executionHours < 0 || executionMinutes < 0 || executionMinutes >= 60) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка: некорректное время выполнения!");
                return "redirect:/services/edit/" + id;
            }

            // Обновляем данные
            existingService.setServiceName(serviceName);
            existingService.setCost(cost);
            existingService.setExecutionHours(executionHours);
            existingService.setExecutionMinutes(executionMinutes);
            existingService.setDescription(description);

            serviceRepository.save(existingService);
            logger.info("Service updated successfully");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Данные услуги успешно обновлены!");

        } catch (IllegalArgumentException e) {
            logger.error("Service not found: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating service: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении услуги");
        }

        return "redirect:/services";
    }
}
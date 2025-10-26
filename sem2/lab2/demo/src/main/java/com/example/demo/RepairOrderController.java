package com.example.demo;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Controller
public class RepairOrderController {
    private List<RepairOrder> repairOrderList = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    // Инициализация некоторых категорий по умолчанию
    public RepairOrderController() {
        categories.add(new Category("Диагностика", "#007bff"));
        categories.add(new Category("ТО", "#28a745"));
        categories.add(new Category("Ремонт двигателя", "#dc3545"));
        categories.add(new Category("Кузовные работы", "#ffc107"));
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String index(Model model,
                        @RequestParam(required = false) String search,
                        Authentication authentication) {
        List<RepairOrder> filteredOrders = repairOrderList;

        // Поиск
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            filteredOrders = repairOrderList.stream()
                    .filter(order ->
                            order.getClientName().toLowerCase().contains(searchLower) ||
                                    order.getCarModel().toLowerCase().contains(searchLower) ||
                                    order.getLicensePlate().toLowerCase().contains(searchLower) ||
                                    order.getDescription().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        model.addAttribute("orders", filteredOrders);

        // Передаем категории всем пользователям для отображения, но управление только MANAGER и ADMIN
        model.addAttribute("categories", categories);

        model.addAttribute("search", search);
        return "orders";
    }


    @PreAuthorize("hasRole('RECEPTION') or hasRole('MANAGER') or hasRole('ADMIN')")
    @PostMapping("/addOrder")
    public String addOrder(@ModelAttribute RepairOrder order,
                           @RequestParam(required = false) Long categoryId) {
        // Установка категории если выбрана
        if (categoryId != null) {
            Category selectedCategory = categories.stream()
                    .filter(cat -> cat.getId().equals(categoryId))
                    .findFirst()
                    .orElse(null);
            order.setCategory(selectedCategory);
        }

        repairOrderList.add(order);
        return "redirect:/orders";
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/deleteOrder/{id}")
    public String deleteOrder(@PathVariable Long id) {
        repairOrderList.removeIf(order -> order.getId().equals(id));
        return "redirect:/orders";
    }

    @PreAuthorize("hasRole('MECHANIC') or hasRole('MANAGER') or hasRole('ADMIN')")
    @PostMapping("/updateOrderStatus/{id}")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam boolean completed) {
        repairOrderList.stream()
                .filter(order -> order.getId().equals(id))
                .findFirst()
                .ifPresent(order -> order.setCompleted(completed));
        return "redirect:/orders";
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PostMapping("/updateDeadline/{id}")
    public String updateDeadline(@PathVariable Long id, @RequestParam LocalDateTime newDeadline) {
        repairOrderList.stream()
                .filter(order -> order.getId().equals(id))
                .findFirst()
                .ifPresent(order -> order.setDeadline(newDeadline));
        return "redirect:/orders";
    }

    // НОВЫЕ МЕТОДЫ

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PostMapping("/addCategory")
    public String addCategory(@RequestParam String name, @RequestParam String color) {
        categories.add(new Category(name, color));
        return "redirect:/orders";
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PostMapping("/updateOrderCategory/{id}")
    public String updateOrderCategory(@PathVariable Long id, @RequestParam(required = false) Long categoryId) {
        repairOrderList.stream()
                .filter(order -> order.getId().equals(id))
                .findFirst()
                .ifPresent(order -> {
                    Category category = categoryId != null ?
                            categories.stream()
                                    .filter(cat -> cat.getId().equals(categoryId))
                                    .findFirst()
                                    .orElse(null) : null;
                    order.setCategory(category);
                });
        return "redirect:/orders";
    }

    @PostMapping("/addComment/{id}")
    public String addComment(@PathVariable Long id,
                             @RequestParam String text,
                             Authentication authentication) {
        repairOrderList.stream()
                .filter(order -> order.getId().equals(id))
                .findFirst()
                .ifPresent(order -> {
                    Comment comment = new Comment(text, authentication.getName(), id);
                    order.addComment(comment);
                });
        return "redirect:/orders";
    }
}
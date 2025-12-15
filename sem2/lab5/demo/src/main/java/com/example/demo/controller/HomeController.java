package com.example.demo.controller;

import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.SparePartRepository;
import com.example.demo.repository.SupplierRepository;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SparePartRepository sparePartRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/")
    public String home(Model model) {
        try {
            // Получаем статистику
            long totalClients = clientRepository.count();
            long totalParts = sparePartRepository.count();
            long totalSuppliers = supplierRepository.count();
            long totalCars = carRepository.count();
            long totalServices = serviceRepository.count();

            // Добавляем атрибуты в модель
            model.addAttribute("totalClients", totalClients);
            model.addAttribute("totalParts", totalParts);
            model.addAttribute("totalSuppliers", totalSuppliers);
            model.addAttribute("totalCars", totalCars);
            model.addAttribute("totalServices", totalServices);

            // Для остальных показателей пока используем заглушки
            model.addAttribute("activeOrders", 0);  // Пока не реализовано

        } catch (Exception e) {
            // В случае ошибки устанавливаем нулевые значения
            model.addAttribute("totalClients", 0);
            model.addAttribute("totalParts", 0);
            model.addAttribute("totalSuppliers", 0);
            model.addAttribute("totalCars", 0);
            model.addAttribute("totalServices", 0);
            model.addAttribute("activeOrders", 0);
        }

        return "html/home";
    }
}
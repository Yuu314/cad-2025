package com.example.demo.controller;

import com.example.demo.entity.Car;
import com.example.demo.entity.Client;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.ClientRepository;
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
@RequestMapping("/cars")
public class CarController {

    private static final Logger logger = LoggerFactory.getLogger(CarController.class);

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ClientRepository clientRepository;

    // ========== ОТОБРАЖЕНИЕ АВТОМОБИЛЕЙ И ПОИСК ==========
    @GetMapping
    public String listCars(@RequestParam(name = "searchType", required = false) String searchType,
                           @RequestParam(name = "searchValue", required = false) String searchValue,
                           Model model) {

        List<Car> cars;
        String searchMessage = "";

        try {
            if (searchValue != null && !searchValue.trim().isEmpty()) {
                // Ручная фильтрация (поиск)
                String trimmedValue = searchValue.trim().toLowerCase();
                List<Car> allCars = carRepository.findAll();

                cars = allCars.stream()
                        .filter(car -> {
                            String type = (searchType != null) ? searchType : "licensePlate";
                            switch (type) {
                                case "licensePlate":
                                    return car.getLicensePlate() != null &&
                                            car.getLicensePlate().toLowerCase().contains(trimmedValue);
                                case "brand":
                                    return car.getBrand() != null &&
                                            car.getBrand().toLowerCase().contains(trimmedValue);
                                case "model":
                                    return car.getModel() != null &&
                                            car.getModel().toLowerCase().contains(trimmedValue);
                                case "vin":
                                    return car.getVin() != null &&
                                            car.getVin().toLowerCase().contains(trimmedValue);
                                case "clientName":
                                    return car.getClient() != null &&
                                            (car.getClient().getLastName().toLowerCase().contains(trimmedValue) ||
                                                    car.getClient().getFirstName().toLowerCase().contains(trimmedValue));
                                default:
                                    return true;
                            }
                        })
                        .collect(Collectors.toList());

                searchMessage = "Результаты поиска";
                logger.info("Search by {}: '{}', found {} cars", searchType, trimmedValue, cars.size());

            } else {
                // Все автомобили
                cars = carRepository.findAll();
                searchMessage = "Все автомобили";
            }

        } catch (Exception e) {
            logger.error("Error loading cars: {}", e.getMessage(), e);
            cars = carRepository.findAll();
            model.addAttribute("errorMessage", "Ошибка при загрузке автомобилей");
            searchMessage = "Ошибка при выполнении поиска";
        }

        model.addAttribute("cars", cars);
        model.addAttribute("searchType", searchType != null ? searchType : "licensePlate");
        model.addAttribute("searchValue", searchValue != null ? searchValue : "");
        model.addAttribute("searchMessage", searchMessage);
        model.addAttribute("totalCars", cars.size());
        model.addAttribute("clients", clientRepository.findAll()); // Для выпадающего списка клиентов

        return "html/cars";
    }

    // ========== ДОБАВЛЕНИЕ АВТОМОБИЛЯ ==========
    @PostMapping("/add")
    public String addCar(@RequestParam("clientId") Long clientId,
                         @RequestParam("licensePlate") String licensePlate,
                         @RequestParam("brand") String brand,
                         @RequestParam("model") String model,
                         @RequestParam(value = "vin", required = false) String vin,
                         @RequestParam(value = "yearOfManufacture", required = false) Integer yearOfManufacture,
                         RedirectAttributes redirectAttributes) {
        try {
            logger.info("Adding car: {} {} (License: {})", brand, model, licensePlate);

            // Проверка существования клиента
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            // Проверка уникальности номера автомобиля
            if (carRepository.existsByLicensePlate(licensePlate)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка: автомобиль с номером '" + licensePlate + "' уже существует!");
                return "redirect:/cars";
            }

            // Проверка уникальности VIN (если указан)
            if (vin != null && !vin.isEmpty() && carRepository.existsByVin(vin)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка: автомобиль с VIN '" + vin + "' уже существует!");
                return "redirect:/cars";
            }

            // Создаем автомобиль
            Car car = new Car();
            car.setClient(client);
            car.setLicensePlate(licensePlate.toUpperCase()); // Приводим к верхнему регистру
            car.setBrand(brand);
            car.setModel(model);
            car.setVin(vin != null ? vin.toUpperCase() : null);
            car.setYearOfManufacture(yearOfManufacture);

            carRepository.save(car);
            logger.info("Car added with ID: {}", car.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Автомобиль " + car.getFullName() + " успешно добавлен!");

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: номер автомобиля или VIN уже существует!");
        } catch (IllegalArgumentException e) {
            logger.error("Client not found: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding car: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении автомобиля");
        }

        return "redirect:/cars";
    }

    // ========== УДАЛЕНИЕ АВТОМОБИЛЯ ==========
    @GetMapping("/delete/{id}")
    public String deleteCar(@PathVariable("id") Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            logger.info("Deleting car with id: {}", id);

            Car car = carRepository.findById(id).orElse(null);

            if (car != null) {
                String carInfo = car.getFullName();
                carRepository.delete(car);
                logger.info("Car {} deleted", carInfo);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Автомобиль " + carInfo + " успешно удалён!");
            } else {
                logger.warn("Car with id {} not found", id);
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Автомобиль с ID " + id + " не найден!");
            }

        } catch (DataIntegrityViolationException e) {
            logger.error("Cannot delete car (foreign key constraint): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Невозможно удалить автомобиль: он связан с заказами или услугами!");
        } catch (Exception e) {
            logger.error("Error deleting car: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении автомобиля");
        }

        return "redirect:/cars";
    }

    // ========== ФОРМА РЕДАКТИРОВАНИЯ ==========
    @GetMapping("/edit/{id}")
    public String editCarForm(@PathVariable("id") Long id, Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            logger.info("Loading edit form for car id: {}", id);

            Car car = carRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));

            model.addAttribute("car", car);
            model.addAttribute("clients", clientRepository.findAll());

            return "html/edit-car";

        } catch (Exception e) {
            logger.error("Error loading edit form: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при загрузке формы: " + e.getMessage());
            return "redirect:/cars";
        }
    }

    // ========== ОБНОВЛЕНИЕ АВТОМОБИЛЯ ==========
    @PostMapping("/update/{id}")
    public String updateCar(@PathVariable("id") Long id,
                            @RequestParam("clientId") Long clientId,
                            @RequestParam("licensePlate") String licensePlate,
                            @RequestParam("brand") String brand,
                            @RequestParam("model") String model,
                            @RequestParam(value = "vin", required = false) String vin,
                            @RequestParam(value = "yearOfManufacture", required = false) Integer yearOfManufacture,
                            RedirectAttributes redirectAttributes) {
        try {
            logger.info("Updating car with id: {}", id);

            // Проверяем существование автомобиля
            Car existingCar = carRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));

            // Проверяем существование клиента
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            // Проверяем уникальность номера (если изменился)
            if (!existingCar.getLicensePlate().equalsIgnoreCase(licensePlate) &&
                    carRepository.existsByLicensePlate(licensePlate)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка: автомобиль с номером '" + licensePlate + "' уже существует!");
                return "redirect:/cars/edit/" + id;
            }

            // Проверяем уникальность VIN (если изменился)
            String newVin = vin != null ? vin.toUpperCase() : null;
            String existingVin = existingCar.getVin();

            if (newVin != null && !newVin.isEmpty() &&
                    (existingVin == null || !existingVin.equalsIgnoreCase(newVin)) &&
                    carRepository.existsByVin(newVin)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка: автомобиль с VIN '" + newVin + "' уже существует!");
                return "redirect:/cars/edit/" + id;
            }

            // Обновляем данные
            existingCar.setClient(client);
            existingCar.setLicensePlate(licensePlate.toUpperCase());
            existingCar.setBrand(brand);
            existingCar.setModel(model);
            existingCar.setVin(newVin);
            existingCar.setYearOfManufacture(yearOfManufacture);

            carRepository.save(existingCar);
            logger.info("Car updated successfully");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Данные автомобиля успешно обновлены!");

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity error on update: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: номер автомобиля или VIN уже существует!");
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating car: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении автомобиля");
        }

        return "redirect:/cars";
    }

    // ========== ПОЛУЧЕНИЕ АВТОМОБИЛЕЙ КЛИЕНТА (AJAX) ==========
    @GetMapping("/by-client/{clientId}")
    @ResponseBody
    public List<Car> getCarsByClient(@PathVariable("clientId") Long clientId) {
        try {
            return carRepository.findByClientId(clientId);
        } catch (Exception e) {
            logger.error("Error getting cars by client: {}", e.getMessage());
            return List.of();
        }
    }
}
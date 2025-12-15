package com.example.demo.controller;

import com.example.demo.entity.SparePart;
import com.example.demo.repository.SparePartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/spareparts")
public class SparePartController {

    private static final Logger logger = LoggerFactory.getLogger(SparePartController.class);

    @Autowired
    private SparePartRepository sparePartRepository;

    // ========== ГЛАВНАЯ СТРАНИЦА И ПОИСК ==========
    @GetMapping
    public String listSpareParts(@RequestParam(name = "searchType", required = false) String searchType,
                                 @RequestParam(name = "searchValue", required = false) String searchValue,
                                 Model model) {

        List<SparePart> spareParts;
        String searchMessage = "";

        try {
            if (searchValue != null && !searchValue.trim().isEmpty()) {
                // Ручная фильтрация (поиск)
                String trimmedValue = searchValue.trim().toLowerCase();
                List<SparePart> allParts = sparePartRepository.findAll();

                spareParts = allParts.stream()
                        .filter(part -> {
                            String type = (searchType != null) ? searchType : "name";
                            switch (type) {
                                case "name":
                                    return part.getPartName() != null &&
                                            part.getPartName().toLowerCase().contains(trimmedValue);
                                case "price":
                                    try {
                                        BigDecimal maxPrice = new BigDecimal(trimmedValue);
                                        return part.getPartCost().compareTo(maxPrice) <= 0;
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                case "stock":
                                    return part.getQuantityInStock() > 0;
                                default:
                                    return true;
                            }
                        })
                        .collect(Collectors.toList());

                searchMessage = "Результаты поиска";
                logger.info("Search by {}: '{}', found {} parts", searchType, trimmedValue, spareParts.size());

            } else {
                // Все запчасти
                spareParts = sparePartRepository.findAll();
                searchMessage = "Все запчасти";
            }

        } catch (Exception e) {
            logger.error("Error loading spare parts: {}", e.getMessage(), e);
            spareParts = sparePartRepository.findAll();
            model.addAttribute("errorMessage", "Ошибка при загрузке запчастей");
            searchMessage = "Ошибка при выполнении поиска";
        }

        model.addAttribute("spareParts", spareParts);
        model.addAttribute("searchType", searchType != null ? searchType : "name");
        model.addAttribute("searchValue", searchValue != null ? searchValue : "");
        model.addAttribute("searchMessage", searchMessage);
        model.addAttribute("totalParts", spareParts.size());

        return "html/spareparts";  // Добавил html/
    }

    // ========== ДОБАВЛЕНИЕ ЗАПЧАСТИ ==========
    @PostMapping("/add")
    public String addSparePart(@RequestParam("partName") String partName,
                               @RequestParam("partCost") BigDecimal partCost,
                               @RequestParam("quantityInStock") Integer quantityInStock,
                               RedirectAttributes redirectAttributes) {
        try {
            SparePart part = new SparePart();
            part.setPartName(partName.trim());
            part.setPartCost(partCost);
            part.setQuantityInStock(quantityInStock != null ? quantityInStock : 0);

            sparePartRepository.save(part);
            logger.info("Spare part added: {} (ID: {})", partName, part.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Запчасть \"" + partName + "\" успешно добавлена!");

        } catch (DataIntegrityViolationException e) {
            logger.error("Error adding spare part: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: возможно, такая запчасть уже существует!");
        } catch (Exception e) {
            logger.error("Error adding spare part: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении запчасти");
        }

        return "redirect:/spareparts";
    }

    // ========== УДАЛЕНИЕ ЗАПЧАСТИ ==========
    @GetMapping("/delete/{id}")
    public String deleteSparePart(@PathVariable("id") Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            logger.info("Deleting spare part with id: {}", id);

            SparePart part = sparePartRepository.findById(id).orElse(null);

            if (part != null) {
                String partName = part.getPartName();
                sparePartRepository.delete(part);
                logger.info("Spare part '{}' deleted", partName);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Запчасть \"" + partName + "\" успешно удалена!");
            } else {
                logger.warn("Spare part with id {} not found", id);
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Запчасть с ID " + id + " не найдена!");
            }

        } catch (DataIntegrityViolationException e) {
            logger.error("Cannot delete spare part (foreign key constraint): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Невозможно удалить запчасть: она используется в заказах!");
        } catch (Exception e) {
            logger.error("Error deleting spare part: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении запчасти");
        }

        return "redirect:/spareparts";
    }

    // ========== ФОРМА РЕДАКТИРОВАНИЯ ==========
    @GetMapping("/edit/{id}")
    public String editSparePartForm(@PathVariable("id") Long id, Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            logger.info("Loading edit form for spare part id: {}", id);

            SparePart part = sparePartRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Запчасть не найдена"));

            model.addAttribute("part", part);
            return "html/edit-sparepart";  // Добавил html/

        } catch (Exception e) {
            logger.error("Error loading edit form: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при загрузке формы: " + e.getMessage());
            return "redirect:/spareparts";
        }
    }

    // ========== ОБНОВЛЕНИЕ ЗАПЧАСТИ ==========
    @PostMapping("/update/{id}")
    public String updateSparePart(@PathVariable("id") Long id,
                                  @RequestParam("partName") String partName,
                                  @RequestParam("partCost") BigDecimal partCost,
                                  @RequestParam("quantityInStock") Integer quantityInStock,
                                  RedirectAttributes redirectAttributes) {
        try {
            logger.info("Updating spare part with id: {}", id);

            SparePart part = sparePartRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Запчасть не найдена"));

            part.setPartName(partName.trim());
            part.setPartCost(partCost);
            part.setQuantityInStock(quantityInStock != null ? quantityInStock : 0);

            sparePartRepository.save(part);
            logger.info("Spare part updated successfully");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Данные запчасти успешно обновлены!");

        } catch (DataIntegrityViolationException e) {
            logger.error("Error updating spare part: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении запчасти!");
        } catch (Exception e) {
            logger.error("Error updating spare part: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении запчасти");
        }

        return "redirect:/spareparts";
    }

    // ========== ПОПОЛНЕНИЕ ЗАПАСОВ ==========
    @PostMapping("/restock/{id}")
    public String restockSparePart(@PathVariable("id") Long id,
                                   @RequestParam("restockQuantity") Integer restockQuantity,
                                   RedirectAttributes redirectAttributes) {
        try {
            SparePart part = sparePartRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Запчасть не найдена"));

            int oldQuantity = part.getQuantityInStock();
            part.increaseStock(restockQuantity);
            sparePartRepository.save(part);

            logger.info("Spare part '{}' restocked: {} → {}",
                    part.getPartName(), oldQuantity, part.getQuantityInStock());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Запас запчасти \"" + part.getPartName() + "\" увеличен: " +
                            oldQuantity + " → " + part.getQuantityInStock() + " шт.");

        } catch (Exception e) {
            logger.error("Error restocking spare part: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при пополнении запасов");
        }

        return "redirect:/spareparts";
    }
}
package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RepairOrderService {
    private final RepairOrderRepository orderRepository;

    @Autowired
    public RepairOrderService(RepairOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Метод для добавления нового заказа
    public void addOrder(RepairOrder order) {
        orderRepository.addOrder(order);
    }

    // Метод для получения всех заказов
    public List<RepairOrder> getAllOrders() {
        return orderRepository.getAllOrders();
    }

    // Метод для удаления заказа по его идентификатору
    public void deleteOrder(Long id) {
        orderRepository.deleteOrder(id);
    }

    // Метод для поиска заказа по ID
    public Optional<RepairOrder> findOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // Метод для получения заказов по имени клиента
    public List<RepairOrder> findOrdersByClientName(String clientName) {
        return orderRepository.findByClientName(clientName);
    }

    // Метод для получения заказов по статусу выполнения
    public List<RepairOrder> findOrdersByStatus(boolean completed) {
        return orderRepository.findByCompletedStatus(completed);
    }

    // Метод для получения заказов по механику
    public List<RepairOrder> findOrdersByMechanic(String mechanic) {
        return orderRepository.findByMechanic(mechanic);
    }

    // Метод для получения просроченных заказов
    public List<RepairOrder> findOverdueOrders() {
        return orderRepository.findOverdueOrders();
    }

    // Метод для обновления заказа
    public void updateOrder(RepairOrder order) {
        orderRepository.updateOrder(order);
    }

    // Метод для обновления статуса заказа
    public boolean updateOrderStatus(Long id, boolean completed) {
        Optional<RepairOrder> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            RepairOrder order = orderOpt.get();
            order.setCompleted(completed);
            orderRepository.updateOrder(order);
            return true;
        }
        return false;
    }

    // Метод для назначения механика
    public boolean assignMechanic(Long id, String mechanic) {
        Optional<RepairOrder> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            RepairOrder order = orderOpt.get();
            order.setAssignedMechanic(mechanic);
            orderRepository.updateOrder(order);
            return true;
        }
        return false;
    }

    // Метод для обновления стоимости
    public boolean updateCost(Long id, double cost) {
        Optional<RepairOrder> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            RepairOrder order = orderOpt.get();
            order.setCost(cost);
            orderRepository.updateOrder(order);
            return true;
        }
        return false;
    }

    // Метод для обновления дедлайна
    public boolean updateDeadline(Long id, java.time.LocalDateTime deadline) {
        Optional<RepairOrder> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            RepairOrder order = orderOpt.get();
            order.setDeadline(deadline);
            orderRepository.updateOrder(order);
            return true;
        }
        return false;
    }
}
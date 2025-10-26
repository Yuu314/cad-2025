package com.example.demo;

import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RepairOrderRepository {
    private List<RepairOrder> orderList = new ArrayList<>();

    // Метод для добавления заказа в репозиторий
    public void addOrder(RepairOrder order) {
        orderList.add(order);
    }

    // Метод для получения всех заказов из репозитория
    public List<RepairOrder> getAllOrders() {
        return orderList;
    }

    // Метод для удаления заказа из репозитория по его идентификатору
    public void deleteOrder(Long id) {
        orderList.removeIf(order -> order.getId().equals(id));
    }

    // Метод для поиска заказа по ID
    public Optional<RepairOrder> findById(Long id) {
        return orderList.stream()
                .filter(order -> order.getId().equals(id))
                .findFirst();
    }

    // Метод для получения заказов по имени клиента
    public List<RepairOrder> findByClientName(String clientName) {
        return orderList.stream()
                .filter(order -> order.getClientName().equalsIgnoreCase(clientName))
                .toList();
    }

    // Метод для получения заказов по статусу выполнения
    public List<RepairOrder> findByCompletedStatus(boolean completed) {
        return orderList.stream()
                .filter(order -> order.isCompleted() == completed)
                .toList();
    }

    // Метод для получения заказов по назначенному механику
    public List<RepairOrder> findByMechanic(String mechanic) {
        return orderList.stream()
                .filter(order -> mechanic.equalsIgnoreCase(order.getAssignedMechanic()))
                .toList();
    }

    // Метод для получения просроченных заказов
    public List<RepairOrder> findOverdueOrders() {
        return orderList.stream()
                .filter(RepairOrder::isOverdue)
                .toList();
    }

    // Метод для обновления заказа
    public void updateOrder(RepairOrder updatedOrder) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getId().equals(updatedOrder.getId())) {
                orderList.set(i, updatedOrder);
                break;
            }
        }
    }
}
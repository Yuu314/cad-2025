import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClientManagerForm extends JFrame {
    private JTable clientsTable;
    private DefaultTableModel tableModel;
    private String authHeader;

    public ClientManagerForm(String authHeader) {
        this.authHeader = authHeader;

        System.out.println("Заголовок авторизации: " + authHeader);

        setTitle("Автосервис - Клиенты");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Создаем таблицу как в методичке
        String[] columns = {"№", "Фамилия", "Имя", "Отчество", "Email", "Телефон"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрещаем редактирование
            }
        };

        clientsTable = new JTable(tableModel);
        clientsTable.setRowHeight(25);
        clientsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        clientsTable.getTableHeader().setBackground(new Color(52, 73, 94));
        clientsTable.getTableHeader().setForeground(Color.WHITE);
        clientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Настройка ширины колонок
        clientsTable.getColumnModel().getColumn(0).setPreferredWidth(30);  // №
        clientsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Фамилия
        clientsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Имя
        clientsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Отчество
        clientsTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Email
        clientsTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Телефон

        JScrollPane scrollPane = new JScrollPane(clientsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Список клиентов"));

        add(scrollPane, BorderLayout.CENTER);

        // Панель для кнопок как в методичке
        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Обновить");
        JButton logoutButton = new JButton("Выйти");

        // Стили кнопок
        refreshButton.setBackground(new Color(52, 152, 219));
        refreshButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);

        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Обработчики
        refreshButton.addActionListener(e -> loadClients());
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        // Загружаем данные
        loadClients();
    }

    private void loadClients() {
        new Thread(() -> {
            try {
                System.out.println("Запрос к: http://localhost:8080/clients/api");

                URL url = new URL("http://localhost:8080/clients/api");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Basic " + authHeader);
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                System.out.println("Код ответа: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder jsonResponse = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        jsonResponse.append(inputLine);
                    }
                    in.close();

                    String json = jsonResponse.toString();
                    System.out.println("Получен JSON, длина: " + json.length() + " символов");

                    // Обновляем UI в EDT
                    SwingUtilities.invokeLater(() -> {
                        parseAndDisplayClients(json);
                    });

                } else {
                    // Читаем тело ошибки
                    BufferedReader err = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = err.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    err.close();

                    System.out.println("Ошибка: " + errorResponse.toString());

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(ClientManagerForm.this,
                                "Ошибка сервера: " + responseCode,
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ClientManagerForm.this,
                            "Ошибка подключения: " + e.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void parseAndDisplayClients(String json) {
        // Очищаем таблицу
        tableModel.setRowCount(0);

        if (json == null || json.trim().isEmpty()) {
            tableModel.addRow(new Object[]{"", "Нет данных", "", "", "", ""});
            return;
        }

        // Проверяем, не HTML ли это
        if (json.contains("<!DOCTYPE html>") || json.contains("<html")) {
            tableModel.addRow(new Object[]{"", "ОШИБКА: HTML вместо JSON", "", "", "", ""});
            tableModel.addRow(new Object[]{"", "Проверьте авторизацию", "", "", "", ""});
            return;
        }

        try {
            // Убираем квадратные скобки
            json = json.trim();
            if (json.startsWith("[") && json.endsWith("]")) {
                json = json.substring(1, json.length() - 1);
            }

            if (json.isEmpty()) {
                tableModel.addRow(new Object[]{"", "Нет клиентов в базе", "", "", "", ""});
                return;
            }

            // Разделяем объекты
            String[] clientsArray = json.split("\\},\\{");

            for (int i = 0; i < clientsArray.length; i++) {
                String clientJson = clientsArray[i];

                // Чистим фигурные скобки
                if (i == 0 && clientJson.startsWith("{")) {
                    clientJson = clientJson.substring(1);
                }
                if (i == clientsArray.length - 1 && clientJson.endsWith("}")) {
                    clientJson = clientJson.substring(0, clientJson.length() - 1);
                }

                // Извлекаем поля
                String lastName = "";
                String firstName = "";
                String patronymic = "";
                String email = "";
                String phone = "";

                // Разделяем поля
                String[] fields = clientJson.split(",");
                for (String field : fields) {
                    String[] parts = field.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim().replace("\"", "");
                        String value = parts[1].trim().replace("\"", "");

                        switch (key) {
                            case "lastName":
                                lastName = value;
                                break;
                            case "firstName":
                                firstName = value;
                                break;
                            case "patronymic":
                                patronymic = value;
                                break;
                            case "email":
                                email = value;
                                break;
                            case "phone":
                                phone = value;
                                break;
                        }
                    }
                }

                // Добавляем строку в таблицу
                Object[] row = {
                        i + 1,         // №
                        lastName,      // Фамилия
                        firstName,     // Имя
                        patronymic,    // Отчество
                        email,         // Email
                        phone          // Телефон
                };
                tableModel.addRow(row);
            }

            if (tableModel.getRowCount() == 0) {
                tableModel.addRow(new Object[]{"", "Не удалось распарсить данные", "", "", "", ""});
            }

        } catch (Exception e) {
            tableModel.addRow(new Object[]{"", "Ошибка парсинга: " + e.getMessage(), "", "", "", ""});
        }
    }

    // Метод для тестирования с жестко заданным JSON
    public static void testWithHardcodedJson() {
        String testJson = "[{\"id\":1,\"lastName\":\"Иванов\",\"firstName\":\"Иван\",\"patronymic\":\"Иванович\",\"email\":\"ivanov@mail.ru\",\"phone\":\"+79161234567\"}," +
                "{\"id\":2,\"lastName\":\"Петрова\",\"firstName\":\"Мария\",\"patronymic\":\"Сергеевна\",\"email\":\"petrova@gmail.com\",\"phone\":\"+79262345678\"}," +
                "{\"id\":3,\"lastName\":\"Сидоров\",\"firstName\":\"Алексей\",\"patronymic\":\"Николаевич\",\"email\":\"sidorov@yandex.ru\",\"phone\":\"+79373456789\"}]";

        SwingUtilities.invokeLater(() -> {
            ClientManagerForm form = new ClientManagerForm("test");
            form.parseAndDisplayClients(testJson);
            form.setVisible(true);
        });
    }

}
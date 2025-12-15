import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginForm() {
        // UI компоненты как в методичке
        setTitle("Автосервис - Вход");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        // Поля ввода для логина
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Войти");

        add(new JLabel("Логин:"));
        add(usernameField);
        add(new JLabel("Пароль:"));
        add(passwordField);
        add(new JPanel()); // Пустое место
        add(loginButton);

        // Обработчик авторизации как в методичке
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Попытка авторизации
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Кодирование в Base64 как в методичке
                String encodedAuth = Base64.getEncoder()
                        .encodeToString((username + ":" + password).getBytes());

                System.out.println("Encoded Auth: " + encodedAuth);

                // Если авторизация успешна, открываем главную форму
                ClientManagerForm clientManagerForm = new ClientManagerForm(encodedAuth);
                clientManagerForm.setVisible(true);
                dispose();  // Закрываем форму авторизации
            }
        });
    }

    public static void main(String[] args) {
        // Запуск как в методичке
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }
}
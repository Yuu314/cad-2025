package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Главная страница доступна всем
                        .requestMatchers("/", "/home").permitAll()
                        // Страница логина доступна всем
                        .requestMatchers("/login").permitAll()
                        // Статические ресурсы доступны всем
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // Доступ к просмотру клиентов — для всех аутентифицированных
                        .requestMatchers("/clients").authenticated()
                        // Добавление, редактирование, удаление — только для ADMIN
                        .requestMatchers("/clients/add", "/clients/edit/**",
                                "/clients/update/**", "/clients/delete/**",
                                "/clients/view/**").hasRole("ADMIN")
                        // Всё остальное — только для авторизованных
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")  // Явно указываем URL для выхода
                        .logoutSuccessUrl("/") // После выхода переходим на главную
                        .invalidateHttpSession(true)  // Отменяем сессию
                        .deleteCookies("JSESSIONID")  // Удаляем куки
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails client = User.builder()
                .username("client")
                .password(encoder.encode("client123"))
                .roles("CLIENT")
                .build();

        return new InMemoryUserDetailsManager(admin, client);
    }
}
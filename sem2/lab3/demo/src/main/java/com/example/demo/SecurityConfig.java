package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // Клиент - может просматривать свои заказы и историю обслуживания
        UserDetails client = User.withDefaultPasswordEncoder()
                .username("client")
                .password("password")
                .roles("CLIENT")
                .build();

        // Приемщик - принимает автомобили, создает заказы-наряды
        UserDetails reception = User.withDefaultPasswordEncoder()
                .username("reception")
                .password("password")
                .roles("RECEPTION")
                .build();

        // Механик - выполняет ремонтные работы, обновляет статусы работ
        UserDetails mechanic = User.withDefaultPasswordEncoder()
                .username("mechanic")
                .password("password")
                .roles("MECHANIC")
                .build();

        // Менеджер - управляет заказами, клиентами, отчетностью
        UserDetails manager = User.withDefaultPasswordEncoder()
                .username("manager")
                .password("password")
                .roles("MANAGER")
                .build();

        // Администратор - полный доступ ко всем функциям системы
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(client, reception, mechanic, manager, admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/login", "/css/**").permitAll()
                        .requestMatchers("/orders").authenticated()
                        .requestMatchers("/addOrder").hasAnyRole("RECEPTION", "MANAGER", "ADMIN")
                        .requestMatchers("/deleteOrder/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/updateOrderStatus/**").hasAnyRole("MECHANIC", "MANAGER", "ADMIN")
                        .requestMatchers("/updateDeadline/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/addCategory", "/updateOrderCategory/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/assignMechanic/**", "/updateCost/**").hasAnyRole("MANAGER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/orders");
        handler.setAlwaysUseDefaultTargetUrl(true);
        return handler;
    }
}
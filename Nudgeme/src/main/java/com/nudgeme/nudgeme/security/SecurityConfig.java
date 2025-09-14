package com.nudgeme.nudgeme.security;

import com.nudgeme.nudgeme.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors->{})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/quotes/**").permitAll()
                        .requestMatchers("/goals/**").permitAll()
                        .requestMatchers("/tasks/**").permitAll()
                        .requestMatchers("/challenges/**").permitAll()
                        .requestMatchers("/challenge-tasks/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/users/**").permitAll()
                        .requestMatchers("/user-profile/**").permitAll()
                        .requestMatchers("/user-stats/**").permitAll()
                        .requestMatchers("/achievements/**").permitAll()
                        .requestMatchers("/user-achievements/**").permitAll()
                        .requestMatchers("/mood/**").permitAll()
                        .requestMatchers("/analytics/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

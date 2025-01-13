//package com.scsb.pm.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers( "/login").permitAll() // 允許所有人去login頁面
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login") // 自定義
//                        .defaultSuccessUrl("/homepage", true)
//                        .permitAll()
//                )
////                .logout(logout -> logout)
////                    .logoutUrl("/logout")
////                    .logoutSuccessUrl("/login?logout")
////                    .permitAll()
//                .csrf(AbstractHttpConfigurer::disable); // 根據需要啟用或禁用 CSRF 防護
//
//        return http.build();
//    }
//}
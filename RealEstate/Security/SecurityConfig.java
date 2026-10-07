package com.project.RealEstate.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http, CustomLoginSuccessHandler successHandler) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/images/**", "/css/**", "/js/**").permitAll()
                                                .requestMatchers(
                                                                "/error/**",
                                                                "/**",
                                                                "/api/userIndex/**",
                                                                "/api/agentIndex/**",
                                                                "/api/ownerIndex/**",
                                                                "/api/allsignup/**",
                                                                "/api/login/**",
                                                                "/api/login?success/**",
                                                                "/api/login?errors/**",
                                                                "/api/login?erroritem/**",
                                                                "/api/user/login/**",
                                                                "/api/signup/**",
                                                                "/api/user/register/**",
                                                                "/api/check-email/**",
                                                                "/verify/**",
                                                                "/resend-verification/**",
                                                                "/api/search/userproperty",
                                                                "/api/user/createOrder/**",
                                                                "/api/user/payment/success",
                                                                "/api/user/subscription/status",
                                                                "/api/logout/**",
                                                                "/api/showAgent/**",
                                                                "/api/agent/image/{id}/**",
                                                                "/api/searchAgents/**",
                                                                "/api/showAgent/profile/{id}/**",
                                                                "/api/agent/agentProperties/**",
                                                                "/api/agent/logout/**",
                                                                "/api/agent/response/**",
                                                                "/api/save/ediprofile/**",
                                                                "/api/agent/updateprofile/**",
                                                                "/api/agent/showsignup/**",
                                                                "/api/agent/agentsignup/**",
                                                                "/api/agentIndex?success/**",
                                                                "/api/agent/enquiries/**",
                                                                "/api/owner/showsignup/**",
                                                                "/api/owner/register/**",
                                                                "/api/owner/logout/**",
                                                                "/api/userIndex?erroritem/**",
                                                                "/api/reply/agentresponse/**",
                                                                "/agent/subscription/status/**",
                                                                "/agent/createOrder/{plan}/**",
                                                                "/agent/payment/success/**",
                                                                "/api/post/enquiry",
                                                                "/api/agent/enquiriesofusers/**",
                                                                "/api/viewAllProperties/**",
                                                                "/api/viewAllProperties/searchProperty/**",
                                                                "/api/property/image/{id}/**",
                                                                "/api/viewAllProperties/details/{id}/**",
                                                                "/api/save/agentreplies/**",
                                                                "/agent/createOrder/**",
                                                                "/agent/payment/**",
                                                                "/agent/subscription/status",
                                                                // OWNER SECTION
                                                                "/api/images/{id}",
                                                                "/api/owner/showAgent",
                                                                "/api/newProject/images/{id}",
                                                                "/api/project-image/{id}",
                                                                "/api/myProperty",
                                                                "/api/edit/{id}",
                                                                "/api/update/property/**",
                                                                "/api/delete/{ids}",
                                                                "/api/view/sold/{pid}/{type}",
                                                                "/api/newProject",
                                                                "/api/save/mewProject",
                                                                "/api/view/newProject",
                                                                "/api/update/newProject/{nId}",
                                                                "/api/update",
                                                                "/api/newProject/delete/{dId}",
                                                                "/owner/homepage",
                                                                "/api/owner/signup",
                                                                "/api/owner/save",
                                                                "/api/subscription",
                                                                "/api/pay/{amount}",
                                                                "/api/save/property/**",
                                                                "/api/save/**",
                                                                "/api/saved/subscription/{orderId}/{amount}",
                                                                "/api/view/transaction",
                                                                "/api/owner/showAgent/profile/{id}",
                                                                "/api/owner/viewAllProperties/details/{id}")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(login -> login
                                                .loginPage("/api/login")
                                                .loginProcessingUrl("/api/user-unused")
                                                .usernameParameter("email")
                                                .passwordParameter("password")
                                                .successHandler(successHandler)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/api/logout")
                                                .logoutSuccessUrl("/")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")); // disable default login page

                return http.build();
        }

        @Bean
        public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
                return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
        }
}

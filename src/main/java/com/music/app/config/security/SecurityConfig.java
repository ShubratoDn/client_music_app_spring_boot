package com.music.app.config.security;

import com.music.app.config.CustomAuthenticationFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;



@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    	 http.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults())
         .authorizeHttpRequests(auth ->
                 auth
                         .requestMatchers("/admin/**").hasRole("ADMIN")
                         .requestMatchers("/user/**").hasRole("USER")
//                         .requestMatchers("/login").anonymous() //
                         .requestMatchers("/register").permitAll()
                         .requestMatchers("/WEB-INF/**").permitAll()
                         .requestMatchers("/js/**").permitAll()
                         .requestMatchers("/css/**").permitAll()
                         .requestMatchers("/assets/**").permitAll()
                         .requestMatchers("/verification", "/forgot-password", "/verify-otp", "/verify-forget-password","/reset-password").permitAll()
                         .anyRequest().authenticated()
         )
         .logout(logout -> logout.logoutUrl("/logout"))
         .formLogin(login -> login
					.loginPage("/login").permitAll()
                 .defaultSuccessUrl("/")
					.failureHandler(new CustomAuthenticationFailureHandler()));
    	

        http.authenticationProvider(myAuthenticationProvider());

        DefaultSecurityFilterChain build = http.build();
        return build;
    }

    @Bean
    public DaoAuthenticationProvider myAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder()); // Set password encoder
        provider.setUserDetailsService(customUserDetailsService()); // Set custom user details service
        return provider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCrypt for password encoding
    }

    @Bean
    public UserDetailsService customUserDetailsService() {
        return new UserDetailsServiceImple(); // Custom implementation of UserDetailsService
    }
}

package org.liudmylamalomuzh.config;

import org.liudmylamalomuzh.entity.User;
import org.liudmylamalomuzh.repository.UserRepository;
import org.liudmylamalomuzh.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfiguration {

    private final UserRepository userRepository;

    public ApplicationConfiguration(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .inMemoryAuthentication()
//                .withUser("user")
//                .password("password")
//                .roles("USER");
//    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

@Bean
        UserDetailsPasswordService userDetailsPasswordService() {
            return (userDetails, newPassword) -> {
                User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                user.setPassword(newPassword);
                userRepository.save(user);
                return user;
            };
        }

    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());

        authProvider.setUserDetailsPasswordService(userDetailsPasswordService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }
}


package com.legoaggelos.catplace;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//@EnableWebSecurity
@EnableWebSecurity(debug = true)
@Configuration
public class SecurityConfig {
	 @Bean
	    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	        http
	                .authorizeHttpRequests(request -> request
	                        .requestMatchers("/cats/**")
	                        .hasRole("CAT-OWNER")
	                       )
	                .httpBasic(Customizer.withDefaults())
	                .csrf(csrf -> csrf.disable());
	        return http.build();
	    }

	    @Bean
	    PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }

	    @Bean
	    InMemoryUserDetailsManager testOnlyUsers(PasswordEncoder passwordEncoder) {
	        User.UserBuilder users = User.builder();
	        UserDetails legoaggelos = users
	                .username("legoaggelos")
	                .password(passwordEncoder.encode("abc123"))
	               
	                .roles("CAT-OWNER")
	                .build();
	        UserDetails hankOwnsNoCards = users
	                .username("hank-owns-no-cats")
	                .password(passwordEncoder.encode("qrs456"))
	                .roles("NON-OWNER")
	                .build();
	        UserDetails kat = users
	                .username("kat")
	                .password(passwordEncoder.encode("xyz789"))
	                .roles("CAT-OWNER")
	                .build();
	        return new InMemoryUserDetailsManager(legoaggelos, hankOwnsNoCards, kat);
	    }
}

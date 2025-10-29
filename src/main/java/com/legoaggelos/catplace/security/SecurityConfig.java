package com.legoaggelos.catplace.security;

import com.legoaggelos.catplace.security.users.CatPlaceUser;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@EnableWebSecurity(debug = true)
@Configuration
public class SecurityConfig {


    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request -> {
                            request
                                    .requestMatchers(HttpMethod.GET, "/cats/**", "/users/**", "/catposts/**", "/catposts/fromCatId/**", "/catposts/fromOwnerId/**", "/cats/fromOwner/**", "/comments/**", "/comments/fromPoster/**", "/comments/fromPostId/**", "/comments/getFromReplyingTo/**")
                                    .permitAll();
                            request
                                    .requestMatchers(HttpMethod.GET, "/comments/getFromCatPoster/**", "/comments/getFromPostUserPoster/**")
                                    .hasRole("ADMIN"); //admin only testing methods
                            request
                                    .requestMatchers(HttpMethod.POST, "/users", "users/**")
                                    .permitAll();
                            request.
                                    requestMatchers(HttpMethod.POST, "/cats/**", "/catposts/**", "/comments/**")
                                    .hasRole("USER");
                            request.
                                    requestMatchers(HttpMethod.DELETE, "/cats/**", "/users/**", "/catposts/**", "/catposts/fromCatId/**", "/catposts/fromOwnerId/**", "/cats/fromOwner/**", "/comments/**", "/comments/deleteByPoster/**", "/comments/deleteByParentComment/", "/comments/deleteByPostCatPoster/", "/comments/deleteByPostUserPoster/")
                                    .hasRole("USER");
                            request.
                                    requestMatchers(HttpMethod.PUT, "/cats/**", "/users/**", "/catposts/**", "/comments/**")
                                    .hasRole("USER");
                        }
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JdbcUserDetailsManager userDetailsService(DataSource dataSource, PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();
        UserDetails legoaggelos =
                users
                        .username("paul")
                        .password(passwordEncoder.encode("abc123"))
                        .roles("USER")
                        .build();
        UserDetails hankOwnsNoCats =
                users
                        .username("hank-owns-no-cats")
                        .password(passwordEncoder.encode("qrs456"))
                        .roles("USER")
                        .build();
        UserDetails kat =
                users
                        .username("kat")
                        .password(passwordEncoder.encode("xyz789"))
                        .roles("USER")
                        .build();
        UserDetails admin =
                users
                        .username("legoaggelos")
                        .password(passwordEncoder.encode("admin"))
                        .roles("ADMIN", "USER")
                        .build();
        var jdbc = new JdbcUserDetailsManager(dataSource);
        jdbc.createUser(kat);
        jdbc.createUser(legoaggelos);
        jdbc.createUser(hankOwnsNoCats);
        jdbc.createUser(admin);
        return jdbc;
    }



}
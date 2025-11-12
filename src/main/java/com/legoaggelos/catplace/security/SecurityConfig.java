package com.legoaggelos.catplace.security;

import com.legoaggelos.catplace.security.jwt.AuthEntryPointJwt;
import com.legoaggelos.catplace.security.jwt.AuthTokenFilter;
import com.legoaggelos.catplace.security.users.CatPlaceUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.sql.DataSource;

@EnableWebSecurity(debug = true)
@Configuration
public class SecurityConfig {

    @Autowired
    CatPlaceUserDetailService userDetailsService;
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(unauthorizedHandler)
                )
                .authorizeHttpRequests(request -> {
                            request
                                    .requestMatchers(HttpMethod.GET, "/cats/**", "/users/**", "/catposts/**", "/catposts/fromCatId/**", "/catposts/fromOwnerId/**", "/cats/fromOwner/**", "/comments/**", "/comments/fromPoster/**", "/comments/fromPostId/**", "/comments/getFromReplyingTo/**", "/likedPost/**", "/likedComment/**")
                                    .permitAll();
                            request
                                    .requestMatchers(HttpMethod.GET, "/comments/getFromPostCatPoster/**", "/comments/getFromPostUserPoster/**", "/likedPost/fromId/**", "/likedComment/fromId/**")
                                    .hasRole("ADMIN"); //admin only testing methods
                            request
                                    .requestMatchers(HttpMethod.POST, "/users", "users/**")
                                    .permitAll();
                            request.
                                    requestMatchers(HttpMethod.POST, "/cats/**", "/catposts/**", "/comments/**", "/likedComment/**", "/likedPost/**")
                                    .hasRole("USER");
                            request.
                                    requestMatchers(HttpMethod.DELETE, "/cats/**", "/users/**", "/catposts/**", "/catposts/fromCatId/**", "/catposts/fromOwnerId/**", "/cats/fromOwner/**", "/comments/**", "/comments/deleteByPoster/**", "/comments/deleteByReplyingTo/", "/comments/deleteByPostCatPoster/", "/comments/deleteByPostUserPoster/", "/likedComment/**", "/likedPost/**")
                                    .hasRole("USER");
                            request.
                                    requestMatchers(HttpMethod.PUT, "/cats/**", "/users/**", "/catposts/**", "/comments/**")
                                    .hasRole("USER");
                            request
                                    .requestMatchers(HttpMethod.PUT, "/likedComment/**", "/likedPost/**")
                                    .denyAll();
                        }
                )
                .httpBasic(Customizer.withDefaults())
                .cors(AbstractHttpConfigurer::disable)
                //.csrf(csrf -> csrf.csrfTokenRepository
                  //      (CookieCsrfTokenRepository.withHttpOnlyFalse())); for prod
                .csrf(AbstractHttpConfigurer::disable);
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
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
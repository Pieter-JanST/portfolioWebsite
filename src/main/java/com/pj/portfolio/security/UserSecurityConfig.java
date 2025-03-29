package com.pj.portfolio.security;

import com.pj.portfolio.service.UserServiceImpl;
import com.pj.portfolio.throttling.ApiRateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class UserSecurityConfig {

    @Autowired
    private UserServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiRateLimitFilter apiRateLimitFilter) throws Exception{
        http
            .addFilterBefore(apiRateLimitFilter, UsernamePasswordAuthenticationFilter.class) // Add rate-limiting filter
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.GET, "/admin", "/admin/").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .httpBasic(Customizer.withDefaults())
//                .csrf(Customizer.withDefaults());
            .csrf(AbstractHttpConfigurer::disable);

        http.formLogin(form ->
                form
                    .loginPage("/login")
                    .loginProcessingUrl("/authenticateUser")
                    .successHandler(new AuthenticationHandler())
                    .failureHandler(new AuthenticationHandler())
                    .permitAll()
        );

        http.logout(logout ->
            logout
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))
                    .logoutSuccessUrl("/login")
                    .permitAll()
        );

        http.exceptionHandling(customizer ->
                customizer
                        .accessDeniedHandler(new AdminAccessDeniedHandler())
        );

        return http.build();
    }
}


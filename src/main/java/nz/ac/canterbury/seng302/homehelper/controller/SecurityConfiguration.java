package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
@ComponentScan("com.baeldung.security")
public class SecurityConfiguration {

    @Autowired
    private CustomAuthenticationProvider authProvider;
    @Autowired
    private UserService userService;

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws  Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }
    @SuppressWarnings("removal")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth.requestMatchers(AntPathRequestMatcher.antMatcher("/h2/**")).permitAll())
                .headers(headers -> headers
                        .frameOptions().disable()
                        // Add CSP header here
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "img-src 'self' data: https://tile.csse.canterbury.ac.nz; " +
                                                "style-src 'self' 'unsafe-inline'; " +
                                                "script-src 'self' 'unsafe-inline' https://code.jquery.com; " +
                                                "font-src 'self' data:"
                                )
                        )
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2/**")))
                .authorizeHttpRequests()
                .requestMatchers("/home","/webjars/**","/", "/registration-form", "/login", "/login-error", "/registration-code","/resources/**", "/css/**", "/images/**", "/forgot-password", "/reset-password", "logout","/suggest")
                .permitAll()
                .requestMatchers("/admin")
                .hasRole("ADMIN")
                .anyRequest()
                .access((authentication, context) -> { //Chatgpt helped me write this
                    if (authentication == null || !authentication.get().isAuthenticated()) {
                        return new AuthorizationDecision(false); // Deny access
                    }
                    var principal = context.getRequest().getUserPrincipal();
                    if (principal == null) {
                        return new AuthorizationDecision(false); // Treat as unauthenticated
                    }
                    String email = principal.getName(); // Get email/username
                    var user = userService.getUser(email); // Fetch user
                    return new AuthorizationDecision(user != null && user.getVerificationCode() == null); // Allow only if code == null
                })
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login-error")
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .and()
                .exceptionHandling()
                .accessDeniedHandler((request,response,accessDeniedException) -> {
                    if (request.getUserPrincipal() != null) {
                        var user = userService.getUser(request.getUserPrincipal().getName());
                        if (user != null && user.getVerificationCode() != null) {
                            response.sendRedirect("/registration-code");
                        }
                    } else {
                        response.sendRedirect("/login-error");
                    }
                });

        return http.build();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

}
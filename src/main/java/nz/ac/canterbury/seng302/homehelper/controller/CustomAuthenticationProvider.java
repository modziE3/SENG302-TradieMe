package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Objects;

/**
 * Custom Authentication Provider class, to allow for handling authentication in any way we see fit.
 * In this case using our existing {@link User}
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
    /**
     * Autowired user service for custom authentication using our own user objects
     */
    @Autowired
    private UserService userService;
    private final ValidationService validationService = new ValidationService();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CustomAuthenticationProvider() {
        super();
    }

    /**
     * Custom authenticaton implementation
     *
     * @param authentication An implementation object that must have non-empty email (name) and password (credentials)
     * @return A new {@link UsernamePasswordAuthenticationToken} if email and password are valid with users authorities
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = String.valueOf(authentication.getName());
        String password = String.valueOf(authentication.getCredentials());
        String errors = "";
        logger.info("email: " + email);
        if (email == null || email.isEmpty() || email.equals("") || password == null || !validationService.checkEmailForm(email)) {
            errors += "Email-form ";
        }

        User u = userService.getUser(email);
        if (u == null ||!passwordEncoder.matches(password, u.getPassword()) || Objects.requireNonNull(password).isEmpty()) {
            errors += "Email-unknown";
        }
        if (errors.isEmpty()) {
            return new UsernamePasswordAuthenticationToken(u.getEmail(), null, u.getAuthorities());
        } else {
            errors += "|" + email;
            logger.error(errors);
            throw new BadCredentialsException(errors);
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}


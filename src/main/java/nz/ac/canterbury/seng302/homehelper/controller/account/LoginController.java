package nz.ac.canterbury.seng302.homehelper.controller.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LoginController {

    Logger logger = LoggerFactory.getLogger(LoginController.class);

    /**
     * GETS the login page for the user.
     * @return the login html.
     */
    @GetMapping(value = "/login")
    public String login(@RequestParam(name = "errorMessageEmail", required = false) String errorMessageEmail,
                        @RequestParam(name = "errorMessagePassword", required = false) String errorMessagePassword,
                        @RequestParam(name = "error", required = false) String errorMessage,
                        @RequestParam(name = "confirmation", required = false) String confirmationMessage,
                        Model model) {
        logger.info("GET /login");
        model.addAttribute("errorMessageEmail", errorMessageEmail);
        model.addAttribute("errorMessagePassword", errorMessagePassword);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.addAttribute("errorMessage", errorMessage);
        }
        if (confirmationMessage != null && !confirmationMessage.isEmpty()) {
            model.addAttribute("confirmationMessage", confirmationMessage);
        }
        return "loginTemplate";
    }

    /**
     * GETS the login page in the case that there is an error. Gets the errors from the user authentication and displays
     * them as error messages on the screen.
     * @param request the Http server
     * @param model the model containing attributes for the html page.
     * @return the login html.
     */
    @GetMapping(value = "/login-error")
    public String loginError(HttpServletRequest request, Model model) {
        logger.info("GET /login-error");
        HttpSession session = request.getSession(false);
        String errorMessageEmail = "";
        String errorMessagePassword = "";
        String userEmail = "";
        if (session != null) {
            AuthenticationException authException = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            List<String> details = List.of(authException.getMessage().split("\\|"));
            if (details.size() > 1) {
                userEmail = details.get(1);
            }
            if (details.get(0) != null) {
                if (details.get(0).contains("Email-unknown")) {
                    errorMessageEmail = "The email address is unknown, or the password is invalid";
                    errorMessagePassword = "The email address is unknown, or the password is invalid";
                }
                if (details.get(0).contains("Email-form")) {
                    errorMessageEmail = "Email address must be in the form ‘jane@doe.nz‘";
                }
            }
        }
        logger.info(errorMessageEmail);
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("errorMessageEmail", errorMessageEmail);
        model.addAttribute("errorMessagePassword", errorMessagePassword);
        logger.info(model.toString());
        return "loginTemplate";
    }

    /**
     * Logs out user with an optional error
     * @param error message displayed on login screen
     * @param confirmation confirmation message displayed on login screen
     * @param request the current session
     * @return redirect to login page
     */
    @GetMapping("/logout")
    public String logout(@RequestParam(name = "error", required = false) String error,
                         @RequestParam(name = "confirmation", required = false) String confirmation,
                         HttpServletRequest request) {
        request.getSession().invalidate();
        logger.info("GET /logout");
        if (error != null && !error.isEmpty()) {
            return "redirect:./login?error=" + error;
        }
        if (confirmation != null && !confirmation.isEmpty()) {
            return "redirect:./login?confirmation=" + confirmation;
        }
        return "redirect:./login";
    }

}

package nz.ac.canterbury.seng302.homehelper.end2end;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

public class LogInFeature {
    @Given("I connect to the system's main URL")
    public void i_connect_to_the_system_s_main_url() {
        PlaywrightCucumberTest.page.navigate(PlaywrightCucumberTest.baseUrl);
    }
    @When("I see the login page")
    public void i_see_the_login_page() {
        Assertions.assertEquals("Login", PlaywrightCucumberTest.page.locator("title").textContent());
    }
    @Then("it includes a button labelled {string}")
    public void it_includes_a_button_labelled(String buttonName) {
        Assertions.assertEquals(buttonName, PlaywrightCucumberTest.page.locator("#loginButton").textContent());
    }


    @Given("I am on the login form and enter an email and corresponding password")
    public void i_am_on_the_login_form_and_enter_an_email_and_corresponding_password() {
        PlaywrightCucumberTest.page.navigate(PlaywrightCucumberTest.baseUrl + "/login");
        PlaywrightCucumberTest.page.locator("#username").type("john@example.com");
        PlaywrightCucumberTest.page.locator("#password").type("P4$$word");
    }
    @When("I click the {string} button")
    public void i_click_the_button(String string) {
        PlaywrightCucumberTest.page.locator("#loginButton").click();
    }

    @Then("I am taken to the main page")
    public void i_am_taken_to_the_main_page() {
        Assertions.assertEquals("Home", PlaywrightCucumberTest.page.locator("title").textContent());
    }

    @Given("I am on the login form")
    public void i_am_on_the_login_form() {
        PlaywrightCucumberTest.page.navigate(PlaywrightCucumberTest.baseUrl + "/login");
    }
    @When("I click the highlighted {string} link")
    public void i_click_the_highlighted_link(String string) {
        PlaywrightCucumberTest.page.locator("#registrationForm").click();
    }
    @Then("I am taken to the registration page")
    public void i_am_taken_to_the_registration_page() {
        Assertions.assertEquals("Sign Up", PlaywrightCucumberTest.page.locator("title").textContent());
    }
}

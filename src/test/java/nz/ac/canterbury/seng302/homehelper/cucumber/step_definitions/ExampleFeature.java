package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

// Temporary step definitions file
public class ExampleFeature {
    @Given("I am on the login page")
    public void i_am_on_the_login_page() {
        Assertions.assertTrue(true);
    }

    @When("I click the {string} link")
    public void i_click_the_link(String string) {
        Assertions.assertTrue(true);
    }

    @Then("I see a form asking me for my email address")
    public void i_see_a_form_asking_me_for_my_email_address() {
        Assertions.assertTrue(true);
    }
}

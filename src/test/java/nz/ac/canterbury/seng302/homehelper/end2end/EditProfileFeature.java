package nz.ac.canterbury.seng302.homehelper.end2end;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

public class EditProfileFeature {
    @Given("I navigate to the edit profile page")
    public void i_navigate_to_the_edit_profile_page() {
        PlaywrightCucumberTest.page.navigate(PlaywrightCucumberTest.baseUrl + "/edit-profile");
    }
    @When("I enter valid values for my first name, last name, and email address")
    public void i_enter_valid_values_for_my_first_name_last_name_and_email_address() {
        PlaywrightCucumberTest.page.locator("#firstName").fill("First");
        PlaywrightCucumberTest.page.locator("#lastName").fill("Last");
        PlaywrightCucumberTest.page.locator("#email").fill("abc@gmail.com");
    }
    @When("I click submit")
    public void i_click_submit() {
        PlaywrightCucumberTest.page.locator("#submitId").click();
    }
    @Then("I am taken back to my profile page")
    public void i_am_taken_back_to_my_profile_page() {
        Assertions.assertEquals("First's Profile", PlaywrightCucumberTest.page.locator("title").textContent());
    }
    @Then("my new details are saved")
    public void my_new_details_are_saved() {
        Assertions.assertEquals(PlaywrightCucumberTest.page.locator("#nameId").textContent(), "Name: First Last");
        Assertions.assertEquals(PlaywrightCucumberTest.page.locator("#emailId").textContent(), "Email: abc@gmail.com");
    }
}

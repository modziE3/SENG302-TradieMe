package nz.ac.canterbury.seng302.homehelper.cucumber;

import io.cucumber.junit.platform.engine.Constants;
import io.cucumber.spring.CucumberContextConfiguration;
import nz.ac.canterbury.seng302.homehelper.HomeHelperApplication;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import org.junit.platform.suite.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.security.Principal;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/integration")
@ConfigurationParameters({
        @ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "nz.ac.canterbury.seng302.homehelper.cucumber"),
        @ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,value = "pretty, html:target/cucumber-report/cucumber.html"),
        @ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
})
@ContextConfiguration(classes = HomeHelperApplication.class)
@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("cucumber")
@AutoConfigureMockMvc
@MockBean(Principal.class)
@MockBean(EmailService.class)
public class RunCucumberTest {
}

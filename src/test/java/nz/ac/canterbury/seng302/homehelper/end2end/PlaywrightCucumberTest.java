package nz.ac.canterbury.seng302.homehelper.end2end;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.junit.platform.engine.Constants;
import io.cucumber.spring.CucumberContextConfiguration;
import nz.ac.canterbury.seng302.homehelper.HomeHelperApplication;
import org.junit.platform.suite.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("end2end")
@ConfigurationParameters({
        @ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "nz.ac.canterbury.seng302.homehelper.end2end"),
        @ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,value = "pretty, html:target/cucumber-report/end2end.html"),
        @ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
})
@ContextConfiguration(classes = HomeHelperApplication.class)
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("end2end")
public class PlaywrightCucumberTest {

    @LocalServerPort
    private int port;
    static Playwright playwright;
    static Browser browser;
    static BrowserContext browserContext;
    static Page page;
    static String baseUrl;


    @BeforeAll
    public static void openResources() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @Before
    public void openContext() {
        baseUrl = "http://localhost:" + port;
        browserContext = browser.newContext();
        page = browserContext.newPage();
        page.navigate(baseUrl);
    }

    @After
    public void closeContext() {
        browserContext.close();
    }

    @AfterAll
    public static void closeResources() {
        playwright.close();
    }
}


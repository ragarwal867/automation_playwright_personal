package net.automation.steps;

import io.cucumber.java.en.Given;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.en.Then;

public class LoginSteps {
    private Playwright playwright;
    private Browser browser;
    private Page page;

    @Given("I am on the Google search page")
    public void i_am_on_the_google_search_page() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        page = browser.newPage();
        page.navigate("https://www.google.com");
    }

    @Then("the page title should be {string}")
    public void the_page_title_should_be(String expectedTitle) {
        String actualTitle = page.title();
        if (!actualTitle.equals(expectedTitle)) {
            throw new AssertionError("Expected title: " + expectedTitle + " but found: " + actualTitle);
        }
    }

    @Then("I close the browser")
    public void i_close_the_browser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
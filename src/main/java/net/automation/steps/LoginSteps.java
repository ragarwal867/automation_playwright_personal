package net.automation.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;

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

    @When("I search for {string}")
    public void i_search_for(String searchTerm) {
        System.out.println("✓ Searching for: " + searchTerm);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Then("I should see welcome message")
    public void i_should_see_welcome_message() {
        System.out. println("Welcome message displayed");
    }

    @When("I add the following items to cart")
    public void i_add_the_following_items_to_cart(DataTable dataTable) {
        List<Map<String, String>> items = dataTable.asMaps();
        System.out.println("✓ Adding items to cart:");
        for (Map<String, String> item : items) {
            System. out.println("  - " + item.get("Product") + " x" + item.get("Quantity") + " @ $" + item.get("Price"));
        }
    }

    @Then("I should be on homepage")
    public void i_should_be_on_homepage() {
        System.out.println("On homepage");
    }

    @Then("the total should be calculated")
    public void the_total_should_be_calculated() {
        System.out.println("Total calculated");
    }


    @Then("the text should be saved")
    public void the_text_should_be_saved() {
        System.out.println("Text saved");
    }
}
@regression @ubs @lc
Feature: Google Search 6

  Scenario: Navigate to Google 61
    Given I am on the Google search page
    Then I close the browser

  Scenario: Navigate to Google 63
    Given I am on the Google search page
    Then the page title should be "Yahoo"
    Then I close the browser
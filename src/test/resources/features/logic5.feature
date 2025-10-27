@regression @galileo @help
Feature: Google Search 5

  Scenario: Navigate to Google 51
    Given I am on the Google search page
    Then I close the browser

  Scenario: Navigate to Google 52
    Given I am on the Google search page
    Then I close the browser

  Scenario: Navigate to Google 53
    Given I am on the Google search page
    Then the page title should be "Yahoo"
    Then I close the browser
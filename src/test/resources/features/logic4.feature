@regression @galileo @administration
Feature: Google Search 4

  Scenario: Navigate to Google 41
    Given I am on the Google search page
    Then I close the browser

  Scenario: Navigate to Google
    Given I am on the Google search page
    Then I close the browser

  Scenario: Navigate to Google 43
    Given I am on the Google search page
    Then I close the browser

  Scenario: Navigate to Google 44
    Given I am on the Google search page
    Then I close the browser

  Scenario: Navigate to Google
    Given I am on the Google search page
    Then the page title should be "Yahoo"
    Then I close the browser
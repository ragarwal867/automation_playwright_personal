@regression @galileo @gtee
Feature: Google Search 7

  Scenario: Navigate to Google 71
    Given I am on the Google search page
    Then I close the browser

  Scenario: Logic 72
    Given I am on the Google search page
    Then the page title should be "Yahoo"
    Then I close the browser

  Scenario: Logic 73
    Given I am on the Google search page
    Then the page title should be "Wrong Title"
    And I close the browser

  Scenario: Logic 74
    Then I should be on homepage
    When I add the following items to cart
      | Product    | Quantity | Price |
      | Laptop     | 1        | 999   |
      | Mouse      | 2        | 25    |
    Then the total should be calculated
    When I add the following items to cart
      | Product    | Quantity | Price |
      | Keyboard   | 1        | 75    |
    Then I should be on homepage
    And I close the browser
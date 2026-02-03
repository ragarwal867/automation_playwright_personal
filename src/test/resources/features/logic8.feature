@regression @galileo @gtee
Feature: Google Search 8

  Scenario: Navigate to Google 81
    Given I am on the Google search page
    Then I close the browser

  Scenario: Logic 82
    Given I am on the Google search page
    Then the page title should be "Yahoo"
    Then I close the browser

  Scenario: Logic 83
    Given I am on the Google search page
    Then the page title should be "Wrong Title"
    And I close the browser

  Scenario: Logic 84
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
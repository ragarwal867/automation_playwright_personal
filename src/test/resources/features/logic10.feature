@regression @galileo @gtee
Feature: Google Search 10

  Scenario: Navigate to Google 101
    Given I am on the Google search page
    Then I close the browser

  Scenario: Logic 102
    Given I am on the Google search page
    Then the page title should be "Yahoo"
    Then I close the browser

  Scenario: Logic 103
    Given I am on the Google search page
    Then the page title should be "Wrong Title"
    And I close the browser

  Scenario: Logic 104
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
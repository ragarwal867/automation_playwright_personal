@regression @galileo @administration
Feature: Google Search 4

  Scenario: Navigate to Google 41
    Given I am on the Google search page
    Then I close the browser

  Scenario: Navigate to Google 42
    Given I am on the Google search page
    Then the page title should be "Yahoo"
    Then I close the browser

  @rla
  Scenario: Logic 43
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
    Then the total should be calculated
    Then I should be on homepage
    Then the total should be calculated
    And I close the browser
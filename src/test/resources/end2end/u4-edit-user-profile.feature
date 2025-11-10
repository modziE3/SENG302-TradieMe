Feature:
  Scenario: Blue Sky?!
    Given I am on the login form and enter an email and corresponding password
    And I click the "Sign In" button
    And I navigate to the edit profile page
    When I enter valid values for my first name, last name, and email address
    And I click submit
    Then I am taken back to my profile page
    And my new details are saved
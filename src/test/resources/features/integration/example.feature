Feature: U8 – Reset password
  Scenario: AC1 - I can see the lost password form when clicking on a link
    Given I am on the login page
    When I click the "Forgot your password?" link
    Then I see a form asking me for my email address
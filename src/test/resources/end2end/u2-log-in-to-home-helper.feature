Feature: U2
  Scenario: AC1
    Given I connect to the system's main URL
    When I see the login page
    Then it includes a button labelled "Login"

  Scenario: AC2
    Given I am on the login form and enter an email and corresponding password
    When I click the "Login" button
    Then I am taken to the main page

  Scenario: AC3
    Given I am on the login form
    When I click the highlighted "Not registered? Create an account" link
    Then I am taken to the registration page

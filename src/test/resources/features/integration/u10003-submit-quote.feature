@SubmitQuote

Feature: U10003 - As a tradie, I want to give a quote to the renovator on a job that has been posted so that I let them know my price and estimated time I am willing to spend on the job
   Scenario: AC1 - I can see the Submit Quote button on the details page of a posted job
    Given I am on the Available Jobs page
    When I go to the details page of a posted job
    Then I can see the Submit Quote button

   Scenario: AC3 - I can create a quote using the Submit Quote form
     Given I am on the Submit Quote form
     When I input valid values and I press Submit
     Then the system saves the quote
     And I am returned to the posted job details page

  Scenario: AC4 - I cannot enter an invalid price in the Submit Quote form
    Given I am on the Submit Quote form
    When I enter empty or invalid values for price and I press Submit
    Then I am shown the price error message

  Scenario: AC5 - I cannot enter an invalid estimated work time in the Submit Quote form
    Given I am on the Submit Quote form
    When I enter empty or invalid values for estimated work time and I press Submit
    Then I am shown the estimated work time error message

  Scenario: AC6 - I cannot enter empty or invalid emails in the Submit Quote form
    Given I am on the Submit Quote form
    When I enter empty or invalid values for email and I press Submit
    Then I am shown the email error message

  Scenario: AC7 - I cannot enter empty or invalid phone number in the Submit Quote form
    Given I am on the Submit Quote form
    When I enter empty or invalid values for phone number and I press Submit
    Then I am shown the phone number error message

  Scenario: AC8
    Given I am on the Submit Quote form
    When I input valid values and I press Submit
    Then The system sends an email to the job owner

  Scenario: AC9
    Given I am on the Submit Quote form
    And I have already submitted a quote
    When I input valid values and I press Submit
    Then I am shown the already quoted error message
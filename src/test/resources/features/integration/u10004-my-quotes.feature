@MyQuotes

Feature: U10004 - As a tradie, I want to see all of the quotes that I have submitted to jobs posted by renovators so that I can keep track of which ones have been approved by renovators
  Scenario: AC1 - I can see a list of quotes I have sent
    Given I am on the My Quotes Page
    When I have created a quote for a job
    Then I can see a list that contains my sent quote

  Scenario: AC5 - I can see a list of quotes I have received
    Given I am on the My Quotes Page
    When Another user creates a quote for a job I own
    Then I can see a list that contains that received quote

  Scenario Outline: AC4 - I can filter quotes by status
    Given I am a logged in user on the My Quotes page
    When I filter quotes by "<status>"
    Then I can see only quotes with that "<status>"
    Examples:
    |status  |
    |Pending |
    |Accepted|
    |Rejected|

  Scenario: AC6.1 - As a user who has received a quote I can accept a quote and retract the job posting
    Given I am on the My Quotes Page
    When Another user creates a quote for a job I own
    Then I can accept the quote and retract the job posting

  Scenario: AC7 - I can reject a quote
    Given I am on the My Quotes Page
    When Another user creates a quote for a job I own
    Then I can reject a quote

  Scenario: AC11 - When I reject a quote, the sender is notified
    Given I am on the My Quotes Page
    When Another user creates a quote for a job I own
    And I reject a quote
    Then The quote sender is sent an email
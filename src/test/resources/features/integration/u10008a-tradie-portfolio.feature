@TradiePortfolio

Feature: U10008a - As a tradie, I want renovators to go to my profile page
  Scenario: AC1
    Given I have sent a quote to a renovators posted job
    When the renovator clicks on the link to my profile
    Then the renovator can see my profile

  Scenario: AC2
    Given I have sent a quote to a renovators posted job
    When the renovator accepts my quote
    And the renovator sets the job to completed
    Then the completed job is added to my completed jobs tab

  Scenario: AC4 - Completed jobs has pagination
    Given I have completed jobs
    When I go on my profile page
    Then The completed jobs are paginated

  Scenario: AC4.2 - Completed jobs pages have 9 jobs each
    Given I have completed jobs
    When I go on my profile page
    Then I am shown upto 9 completed jobs

  Scenario: AC4.3 - Portfolio jobs has pagination
    Given I have portfolio jobs
    When I go on my profile page
    Then The portfolio jobs are paginated

  Scenario: AC4.4 - Portfolio jobs pages have 9 jobs each
    Given I have portfolio jobs
    When I go on my profile page
    Then I am shown upto 9 portfolio jobs
@JobCalender

Feature: U23 - As Kaia, I want to see a calendar displaying my upcoming jobs, so I know which ones need to be prioritised
  Scenario: AC4 - I can go to the edit job form from the job calendar
    Given I am viewing a calendar of a record I own
    When I double-click a job
    Then I am taken to the edit job page for that job
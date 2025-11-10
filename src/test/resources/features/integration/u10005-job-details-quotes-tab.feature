@JobDetailsTab

Feature: U10005 - As Sarah, I want to view the quotes for a job I posted so that I can accept a quote which I like
  Scenario: AC1
    Given I am on the details page of a job I've posted
    When I click on the quotes tab
    Then I can see all the quotes which have been offered

  Scenario: AC3
    Given I am on the details page of a job I've posted
    When I click on the quotes tab
    Then I can reject a quote on the tab

  Scenario: AC4
    Given I am on the details page of a job I've posted
    When I click on the quotes tab
    And I accept a quote on the tab and transfer to expense
    Then A new expense is created for the quote tab

  Scenario: AC5
    Given I am on the details page of a job I've posted
    When I click on the quotes tab
    Then I can accept a quote on the tab

  Scenario: AC7
    Given I am on the details page of a job I've posted
    When I click on the quotes tab
    And I accept a quote on the tab
    Then The quote tab sender is sent an accepted email

  Scenario: AC8
    Given I am on the details page of a job I've posted
    When I click on the quotes tab
    And I reject a quote on the tab
    Then The quote tab sender is sent an email
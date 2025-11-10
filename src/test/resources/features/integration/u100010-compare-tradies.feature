@CompareTradies

Feature: U100010 - As a renovator I want to be able to compare tradies when viewing quotes on any quote page so that I can find the best tradie to work on a job
  Scenario: AC6: Given I am on the compare tradies page, when I reject a tradie, their quote is also rejected.
    Given I am on the compare tradies page
    When I reject a tradie
    Then their quote is also rejected
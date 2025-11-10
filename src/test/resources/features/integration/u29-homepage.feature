@Homepage

Feature: U29 - As Sarah, I want to see a home page with relevant information about aspects of Home Helper I interact with.
  Scenario: Recently viewed jobs show on my homepage widget.
    Given I am viewing a jobs details
    When I go to the homepage
    Then I can see a list of recently viewed jobs containing the job I viewed last

  Scenario: AC4 - I can change the order that widgets appear on the home page
    Given I am on the Customise Widgets page
    When I submit a new home page widget order
    Then Widget order is displayed on the home page

  Scenario Outline: AC5.1 - I can disable a widget so it does not appear on the home page
    Given I am on the Customise Widgets page
    When I disable the home page widget "<widget>"
    Then "<widget>" is not displayed on the home page
    Examples:
      | widget                      |
      | Job Recommendations         |
      | Job Calendar                |
      | Recent Jobs                 |
      | Recently Viewed Renovations |

  Scenario Outline: AC5.2 - I can enable a widget so it does appear on the home page
    Given I am on the Customise Widgets page
    And Home page widget "<widget>" is disabled
    When I enable the home page widget "<widget>"
    Then "<widget>" is displayed on the home page
    Examples:
      | widget                      |
      | Job Recommendations         |
      | Job Calendar                |
      | Recent Jobs                 |
      | Recently Viewed Renovations |
@SearchPage

Feature: U19 - As Inaya, I want to be able to make my renovation record public so that I can share my progress with other.
  Scenario: AC1 - On the renovation record details page, I can set a renovation record to public
    Given I have created a renovation record with public status set to "false"
    When I toggle a switch labeled Public and Private to highlight "Public"
    Then the public status of the renovation record will be set to "true"

  Scenario: AC2 - On the renovation record details page, I can set a renovation record to private
    Given I have created a renovation record with public status set to "true"
    When I toggle a switch labeled Public and Private to highlight "Private"
    Then the public status of the renovation record will be set to "false"

  Scenario: AC3 - I am anywhere on the system and I press the 'Browse Renovations' button
    Given I am logged in
    When I press the Browse Renovations button
    Then I see a list of public renovation records sorted by more recently created ones in descending order.

  Scenario Outline: AC7
    Given I see the list of public renovation records
    And there are more than ten pages
    When I input page number <page number> that is within the range of available pages
    And I confirm that I want to go to page <page number>
    Then I go to the list of renovation records corresponding to page <page number>
    Examples:
      | page number |
      | 1           |
      | 2           |
      | 3           |
      | 4           |
      | 5           |
      | 6           |
      | 7           |
      | 8           |
      | 9           |
      | 10          |
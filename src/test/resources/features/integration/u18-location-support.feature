Feature: U18 – As Kaia, I want to be able to add location to my profile and my renovation records so that I can keep track of where they are.
  Scenario: AC1 - I am on the registration page and can optionally add a location
    Given I register to the system on the registration page
    When I am asked to supply my details on the registration page
    Then I can optionally supply my location on the registration page

  Scenario: AC2 - I am on the edit profile page and can optionally edit my location
    Given I edit my profile on the edit profile page
    When I am asked to supply my location details on the edit profile page
    Then I can optionally supply my location on the edit profile page

  Scenario: AC3 - I am on the add renovation record page and can optionally add a location for it
    Given I create a renovation record on the add renovation record page
    When I am asked to supply the renovation details on the add renovation record page
    Then I can optionally supply a location for that renovation on the add renovation record page

  Scenario: AC4 - I am on the edit renovation record page and can optionally edit the location for it
    Given I edit a renovation record on the edit renovation record page
    When I am asked to supply the renovation details on the edit renovation record page
    Then I can optionally supply a location for that renovation on the edit renovation record page

  Scenario: AC11
    Given I supply a fully compliant address
    When I submit the form
    Then The form is saved with the address I supplied
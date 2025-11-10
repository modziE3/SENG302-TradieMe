Feature: U22 - As Sarah, I want to be able to search for public renovation records by tags so that I can find renovations that
are matching my interest.
   Scenario: AC1 - I can enter a search string and get a list of matching tag names
    Given I enter a search string in the search renovation bar
    When The search string partially matches a tag known by the system
    Then I can see the list of matching tags

  Scenario: AC2 - I can select a tag from the matching tags list and add it to the search bar
    Given I see a list of matching tags for my search query
    When I select a tag from the list
    Then The tag is added to the content of the search bar

  Scenario: AC3 - I can delete a tag that was added to the search bar
    Given I see the tags "Tag 1", "Tag 2", and "Tag 3" in the search bar
    When I click the X next to "Tag 2"
    Then "Tag 2" is removed from the search content and "Tag 1" and "Tag 3" are left

  Scenario: AC5 - I cannot add more than five tags to the search bar
    Given I have added five tags to the search bar
    When I try to add a new tag to the search bar
    Then I get an error message saying you cannot add more than five tags
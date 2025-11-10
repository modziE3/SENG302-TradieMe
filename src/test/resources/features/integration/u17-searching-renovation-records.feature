Feature: U17 - As Kaia, I want to be able to search within my renovation records so that I can find renovation records that
  I have carried in one of my rentals that I can reuse for later.

  Scenario: AC2 - Given I enter a search string in the search bar, when I click a search button either labelled “search”
  or with a magnifying glass icon, then I am shown only my renovation records whose name or description include my search value.
    Given I am on the search renovations page
    When I enter a search string "3" and search for it
    Then I am only shown my renovation records whose name or description include my search value

  Scenario: AC5 - Given I have run a search, when there are more records than the screen can handle,
  then I see pagination buttons, and the results are split into pages.
    Given I am on the search renovations page
    When I enter a search string "e" and search for it
    Then I see pagination buttons and the results are split into pages

  Scenario: AC7 - Given I see the list of records, and pagination numbers, when there are more than 10 pages, then I
  see a button to access the first page, and I see a button to access the last page, and I see buttons to access at most
  2 pages before the page I am currently on, and I see buttons to access at most 2 pages after the page I am currently on.
    Given I am on the search renovations page
    When I enter a search string "e" and search for it
    Then I see pagination buttons set up for more than ten pages
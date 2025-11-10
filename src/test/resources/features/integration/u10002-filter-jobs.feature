@FilterJobs

Feature: U10002
  Scenario Outline: AC1 - Filter jobs by search string
    Given I am on the Available Jobs page.
    When I enter "<searchString>" in the search bar and click filter results
    Then I am shown only posted jobs whose name or description include "<searchString>"
    Examples:
      | searchString |
      | job          |
      |              |
      | 😀           |
      | ob           |
      | job1         |

  Scenario Outline: AC2 - Filter jobs by job type
    Given I am on the Available Jobs page.
    When I select the job type "<jobType>" and click filter results
    Then I am shown only posted jobs whose job type is "<jobType>"
    Examples:
      | jobType    |
      | Carpentry  |
      | Electrical |
      | Plumbing   |
      | Insulating |
      | Plastering |
      | Painting   |

  Scenario Outline: AC3 - Filter jobs by due date
    Given I am on the Available Jobs page.
    When I enter "<dueDate>" in the due date field and click filter results
    Then I am shown only posted jobs whose due date is before "<dueDate>"
    Examples:
      | dueDate    |
      | 01/01/2026 |
      | 01/01/3026 |

  Scenario Outline: AC4.1 - I cannot enter due dates with invalid format
    Given I am on the Available Jobs page.
    When I enter "<dueDate>" in the due date field and click filter results
    Then I am shown the due date error message "Due date is not in valid format, DD/MM/YYYY"
    Examples:
      | dueDate    |
      | 2026-01-01 |
      | 1/1/2026   |
      | 01-01-2026 |

  Scenario Outline: AC4.2 - I cannot enter start dates with invalid format
    Given I am on the Available Jobs page.
    When I enter "<startDate>" in the start date field and click filter results
    Then I am shown the start date error message "Start date is not in valid format, DD/MM/YYYY"
    Examples:
      | startDate    |
      | 2026-01-01   |
      | 1/1/2026     |
      | 01-01-2026   |

    Scenario Outline: AC5.1 - I cannot enter due dates in the past
      Given I am on the Available Jobs page.
      When I enter "<dueDate>" in the due date field and click filter results
      Then I am shown the due date error message "Due date must be in the future"
      Examples:
        | dueDate    |
        | 01/01/2024 |
        | 06/08/2025 |
        | 31/12/2024 |

  Scenario Outline: AC5.2 - I cannot enter start dates in the past
    Given I am on the Available Jobs page.
    When I enter "<startDate>" in the start date field and click filter results
    Then I am shown the start date error message "Start date must be in the future"
    Examples:
      | startDate  |
      | 01/01/2024 |
      | 06/08/2025 |
      | 31/12/2024 |

  Scenario Outline: AC6.1 - Filter jobs by city
    Given I am on the Available Jobs page.
    When I select the city "<city>" and click filter results
    Then I am shown only posted jobs whose city is "<city>"
    Examples:
      | city         |
      | Christchurch |
      | Auckland     |

  Scenario Outline: AC6.2 - Filter jobs by city and suburb
    Given I am on the Available Jobs page.
    When I select the city "<city>" and suburb "<suburb>" and click filter results
    Then I am shown only posted jobs whose city is "<city>" and suburb is "<suburb>"
    Examples:
      | city         | suburb           |
      | Christchurch | Ilam             |
      | Auckland     | Epsom            |
      | Auckland     | Auckland Central |

  Scenario Outline: AC7 - I cannot enter invalid suburb names
    Given I am on the Available Jobs page.
    When I select the city "<city>" and suburb "<invalidSuburb>" and click filter results
    Then I am shown the suburb error message "Suburb contains invalid characters"
    Examples:
      | city         | invalidSuburb      |
      | Christchurch | Ilam#              |
      | Auckland     | \"Epsom\"          |
      | Auckland     | Auckland! Central! |

  Scenario Outline: AC8 - I cannot enter invalid city names
    Given I am on the Available Jobs page.
    When I select the city "<invalidCity>" and suburb "<suburb>" and click filter results
    Then I am shown the city error message "City contains invalid characters"
    Examples:
      | invalidCity   | suburb           |
      | Christchurch2 | Ilam             |
      | \"Auckland\"  | Epsom            |
      | Auckland!     | Auckland Central |

  Scenario: AC9 - I can clear job filters I have entered
    Given I am on the Available Jobs page.
    And I have added some job filters
    When I click the Clear Filters button
    Then All job filters are cleared
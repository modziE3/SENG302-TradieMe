@TradieRatings

Feature: U10007 - As Sarah, I want to be able to leave a rating so that I can give feedback on how helpful a tradie was at completing my renovation job
  Scenario Outline: AC3.1 - Submit rating to tradie with no ratings
    Given A tradie has no ratings
    And I submit a rating of <rating> to a tradie
    When I go to the tradie's profile page
    Then The average rating on their profile page is <rating>
    Examples:
      | rating |
      | 1      |
      | 2      |
      | 3      |
      | 4      |
      | 5      |

  Scenario Outline: AC3.2 - Submit rating to tradie with existing ratings
    Given A tradie has a rating of <tradieRating>
    And I submit a rating of <submitRating> to a tradie
    When I go to the tradie's profile page
    Then The average rating on their profile page is <newAverageRating>
    Examples:
      | tradieRating | submitRating | newAverageRating |
      | 1            | 5            | 3                |
      | 4            | 3            | 3.5              |
      | 5            | 5            | 5                |
      | 3            | 2            | 2.5              |
      | 3            | 1            | 2                |

  Scenario Outline: AC6 - Submitting a 0 rating does not rate the tradie
    Given A tradie has no ratings
    When I submit a rating of <submitRating> to a tradie
    Then Tradie has <ratingNumber> ratings
    Examples:
      | submitRating |  ratingNumber  |
      | 0            |  0             |

  Scenario Outline: AC4 - I cannot submit multiple ratings for a tradie in one job
    Given I submit a rating of <submitRating> to a tradie
    When I set the job status to complete
    Then The tradie is not shown
    Examples:
      | submitRating |
      | 3            |
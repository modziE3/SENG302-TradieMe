@TradieJobMap

Feature: U10009 - As a renovator when I view a tradies profile I want to be able see the locations which they have done work on a map
  Scenario: AC1: given I am on tradie's profile, when I go to the bottom of the profile, then I am shown a map with pins showing the locations of where the tradie has worked on jobs
    Given a tradie has completed jobs in their portfolio
    When I go to that tradie's map
    Then I can see pins on the locations of the portfolio jobs
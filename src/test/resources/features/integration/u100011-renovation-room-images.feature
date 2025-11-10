@RenovationRoomImages

Feature: U100011 - As a renovator I want to be able to edit the image of the room in my renovation record so that tradies can see what rooms will be worked on for the renovation
  Scenario Outline: AC2 - I can submit a valid image for my room
    Given I choose a valid image of type "<validType>" for my room
    When I submit the image
    Then the image for my room is updated
    Examples:
      | validType     |
      | image/png     |
      | image/jpeg    |
      | image/svg+xml |

  Scenario Outline: AC3 - I cannot submit an image of invalid type for my room
    Given I choose an invalid image of type "<invalidType>" for my room
    When I submit the image
    Then the image for my room is not updated and error message "Image must be of type png, jpg or svg" is shown
    Examples:
      | invalidType |
      | image/gif   |
      | image/bmp   |
      | image/tiff  |
@api
Feature: Retrieve a pet by its ID
  As a consumer of the Petstore API
  I want to fetch a pet using its unique ID
  So that I can show accurate pet details to my users

  Endpoint under test: GET /pet/{petId}
  Each scenario creates its own test data and removes it afterwards.

  @smoke @positive
  Scenario: Retrieve an existing pet
    Given a pet named "Rex" with status "available" exists in the store
    When I request the pet by its ID
    Then the response status should be 200
    And the response should be in JSON format
    And the returned pet should match the pet that was created

  @negative
  Scenario: Request a pet that does not exist
    Given no pet exists with a newly generated ID
    When I request the pet by its ID
    Then the response status should be 404
    And the error message should be "Pet not found"

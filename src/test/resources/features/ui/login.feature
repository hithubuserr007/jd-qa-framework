@ui
Feature: OrangeHRM login
  As an OrangeHRM user
  I want to sign in and out securely
  So that only authorised people can access HR data

  Background:
    Given I am on the OrangeHRM login page

  @smoke @positive
  Scenario: Admin logs in and logs out successfully
    When I log in as the admin user
    Then I should see the "Dashboard" page
    And my user name should be shown in the top bar
    When I log out
    Then I should be back on the login page

  # The same message is shown whether the username or the password is wrong,
  # so the application does not reveal which accounts exist.
  @negative
  Scenario: Login is rejected when the password is wrong
    When I attempt to log in with username "Admin" and password "wrongPassword1"
    Then I should see the error message "Invalid credentials"
    And I should remain on the login page

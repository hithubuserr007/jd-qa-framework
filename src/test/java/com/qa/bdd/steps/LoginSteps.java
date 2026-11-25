package com.qa.bdd.steps;

import com.qa.bdd.context.UiContext;
import com.qa.ui.data.UserCredentials;
import com.qa.ui.pages.DashboardPage;
import com.qa.ui.support.UrlPatterns;
import com.qa.ui.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Step definitions for the OrangeHRM login feature.
 * Steps drive the existing page objects (LoginPage, DashboardPage, TopBar) and verify with
 * Playwright's auto-waiting web-first assertions, so there are no sleeps or manual waits.
 */
public class LoginSteps {

    private static final Pattern LOGIN_URL = UrlPatterns.endsWith(LoginPage.PATH);
    private static final Pattern DASHBOARD_URL = UrlPatterns.endsWith(DashboardPage.PATH);

    private final UiContext ui;

    public LoginSteps(UiContext ui) {
        this.ui = ui;
    }

    // ------------------------------------------------------------------ Given

    @Given("I am on the OrangeHRM login page")
    public void iAmOnTheLoginPage() {
        ui.setLoginPage(new LoginPage(ui.page()).open());
    }

    // ------------------------------------------------------------------- When

    @When("I log in as the admin user")
    public void iLogInAsTheAdminUser() {
        ui.setDashboardPage(ui.loginPage().loginAs(UserCredentials.admin()));
    }

    @When("I attempt to log in with username {string} and password {string}")
    public void iAttemptToLogIn(String username, String password) {
        ui.loginPage().attemptLogin(username, password);
    }

    @When("I log out")
    public void iLogOut() {
        ui.setLoginPage(ui.dashboardPage().topBar().logout());
    }

    // ------------------------------------------------------------------- Then

    @Then("I should see the {string} page")
    public void iShouldSeeThePage(String pageTitle) {
        assertThat(ui.page()).hasURL(DASHBOARD_URL);
        assertThat(ui.dashboardPage().header()).hasText(pageTitle);
    }

    @Then("my user name should be shown in the top bar")
    public void myUserNameShouldBeShown() {
        assertThat(ui.dashboardPage().topBar().loggedInUserName()).isVisible();
    }

    @Then("I should see the error message {string}")
    public void iShouldSeeTheErrorMessage(String message) {
        assertThat(ui.loginPage().errorAlert()).hasText(message);
    }

    @Then("I should remain on the login page")
    @Then("I should be back on the login page")
    public void iShouldBeOnTheLoginPage() {
        assertThat(ui.page()).hasURL(LOGIN_URL);
        assertThat(ui.loginPage().loginButton()).isVisible();
    }
}

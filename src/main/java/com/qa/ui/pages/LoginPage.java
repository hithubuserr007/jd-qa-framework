package com.qa.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.qa.reporting.ReportLog;
import com.qa.ui.data.UserCredentials;

/**
 * OrangeHRM login page.
 *
 * Locator strategy, in order of preference: user-facing attributes (role, accessible name,
 * placeholder) > stable attributes (name) > CSS classes. OrangeHRM's generated "oxd-*" classes
 * are only used for the alert / validation text, which has no better hook.
 */
public class LoginPage extends BasePage {

    public static final String PATH = "/web/index.php/auth/login";

    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorAlert;
    private final Locator fieldValidationMessages;

    public LoginPage(Page page) {
        super(page);
        this.usernameInput = page.locator("input[name='username']");
        this.passwordInput = page.locator("input[name='password']");
        this.loginButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
        this.errorAlert = page.locator(".oxd-alert-content-text");
        this.fieldValidationMessages = page.locator(".oxd-input-field-error-message");
    }

    public LoginPage open() {
        ReportLog.step("Open login page");
        page.navigate(PATH);
        usernameInput.waitFor();
        return this;
    }

    /** Happy path: returns the next page in the journey (fluent page-object navigation). */
    public DashboardPage loginAs(UserCredentials user) {
        submit(user.username(), user.password());
        return new DashboardPage(page);
    }

    /** Unhappy path: stays on this page so the test can inspect error state. */
    public LoginPage attemptLogin(String username, String password) {
        submit(username, password);
        return this;
    }

    private void submit(String username, String password) {
        ReportLog.step("Log in as '" + username + "'");
        usernameInput.fill(username == null ? "" : username);
        passwordInput.fill(password == null ? "" : password);
        loginButton.click();
    }

    public Locator usernameInput() {
        return usernameInput;
    }

    public Locator loginButton() {
        return loginButton;
    }

    public Locator errorAlert() {
        return errorAlert;
    }

    public Locator fieldValidationMessages() {
        return fieldValidationMessages;
    }
}

package com.qa.bdd.context;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.qa.ui.pages.DashboardPage;
import com.qa.ui.pages.LoginPage;

/**
 * Per-scenario state for UI scenarios: the isolated browser context, its page,
 * and the page objects the scenario is currently on.
 */
public class UiContext {

    private BrowserContext browserContext;
    private Page page;
    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    public void init(BrowserContext browserContext, Page page) {
        this.browserContext = browserContext;
        this.page = page;
    }

    public BrowserContext browserContext() {
        return browserContext;
    }

    public Page page() {
        return page;
    }

    public LoginPage loginPage() {
        return loginPage;
    }

    public void setLoginPage(LoginPage loginPage) {
        this.loginPage = loginPage;
    }

    public DashboardPage dashboardPage() {
        return dashboardPage;
    }

    public void setDashboardPage(DashboardPage dashboardPage) {
        this.dashboardPage = dashboardPage;
    }
}

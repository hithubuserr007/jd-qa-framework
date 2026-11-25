package com.qa.ui.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.qa.reporting.ReportLog;
import com.qa.ui.pages.LoginPage;

/**
 * Component object for the header bar shown on every authenticated OrangeHRM page.
 * Modelled once and reused by any page object that contains it.
 */
public class TopBar {

    private final Page page;
    private final Locator userDropdown;
    private final Locator loggedInUserName;

    public TopBar(Page page) {
        this.page = page;
        this.userDropdown = page.locator(".oxd-userdropdown-tab");
        this.loggedInUserName = page.locator(".oxd-userdropdown-name");
    }

    public Locator loggedInUserName() {
        return loggedInUserName;
    }

    public LoginPage logout() {
        ReportLog.step("Log out via user menu");
        userDropdown.click();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Logout"))
                .or(page.locator("a[href*='auth/logout']"))
                .first()
                .click();
        return new LoginPage(page);
    }
}

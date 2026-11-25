package com.qa.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.qa.ui.components.TopBar;

public class DashboardPage extends BasePage {

    public static final String PATH = "/web/index.php/dashboard/index";

    private final Locator header;
    private final TopBar topBar;

    public DashboardPage(Page page) {
        super(page);
        this.header = page.locator(".oxd-topbar-header-breadcrumb h6");
        this.topBar = new TopBar(page);
    }

    public Locator header() {
        return header;
    }

    /** Shared layout component, composed rather than inherited (it appears on every authenticated page). */
    public TopBar topBar() {
        return topBar;
    }
}

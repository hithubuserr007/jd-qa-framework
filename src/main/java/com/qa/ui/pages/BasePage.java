package com.qa.ui.pages;

import com.microsoft.playwright.Page;

/**
 * Common behaviour for all page objects.
 *
 * Page objects expose intent-level actions (loginAs, logout) and Locators. They do not contain
 * assertions: tests own the verification, using Playwright's auto-retrying web-first assertions
 * on the Locators exposed here. This keeps pages reusable for both positive and negative tests.
 */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    public Page page() {
        return page;
    }

    public String currentUrl() {
        return page.url();
    }
}

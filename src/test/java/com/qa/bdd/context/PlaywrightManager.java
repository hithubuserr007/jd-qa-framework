package com.qa.bdd.context;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.qa.config.Config;

/**
 * Owns the expensive, long-lived Playwright objects for the whole Cucumber run:
 * one Playwright instance and one browser process, created lazily on first use
 * (API-only runs never launch a browser) and closed once at the end of the run.
 *
 * Per-scenario objects (APIRequestContext, BrowserContext, Page) are created in the hooks,
 * so every scenario is isolated.
 */
public final class PlaywrightManager {

    private static Playwright playwright;
    private static Browser browser;

    private PlaywrightManager() {
    }

    public static synchronized Playwright playwright() {
        if (playwright == null) {
            playwright = Playwright.create();
        }
        return playwright;
    }

    public static synchronized Browser browser() {
        if (browser == null) {
            BrowserType type = switch (Config.browser().toLowerCase()) {
                case "firefox" -> playwright().firefox();
                case "webkit" -> playwright().webkit();
                default -> playwright().chromium();
            };
            browser = type.launch(new BrowserType.LaunchOptions()
                    .setHeadless(Config.headless())
                    .setSlowMo(Config.slowMoMillis()));
        }
        return browser;
    }

    public static synchronized void shutdown() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }
}

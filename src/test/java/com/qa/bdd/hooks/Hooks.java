package com.qa.bdd.hooks;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.bdd.context.ApiContext;
import com.qa.bdd.context.PlaywrightManager;
import com.qa.bdd.context.UiContext;
import com.qa.config.Config;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;

import java.nio.file.Path;
import java.util.Map;

/**
 * Scenario lifecycle, selected by feature tag:
 *  - @api : fresh APIRequestContext per scenario, created pets deleted afterwards
 *  - @ui  : fresh BrowserContext + Page per scenario (no shared cookies/session),
 *           Playwright trace recorded; on failure a screenshot is embedded in the
 *           Cucumber HTML report and the trace is saved to target/traces/
 *
 * Unlike JUnit's @AfterEach, Cucumber's @After runs while the page is still open,
 * so failure evidence can be captured here directly.
 */
public class Hooks {

    private final ApiContext api;
    private final UiContext ui;

    public Hooks(ApiContext api, UiContext ui) {
        this.api = api;
        this.ui = ui;
    }

    @BeforeAll
    public static void beforeAll() {
        // The public OrangeHRM demo can be slow; give web-first assertions more headroom than the 5s default.
        PlaywrightAssertions.setDefaultAssertionTimeout(10_000);
    }

    @AfterAll
    public static void afterAll() {
        PlaywrightManager.shutdown();
    }

    // ------------------------------------------------------------------ API

    @Before("@api")
    public void startApiScenario() {
        api.init(PlaywrightManager.playwright().request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(Config.petstoreBaseUrl())
                .setExtraHTTPHeaders(Map.of("Accept", "application/json"))
                .setTimeout(30_000)));
    }

    @After("@api")
    public void finishApiScenario(Scenario scenario) {
        try {
            if (scenario.isFailed() && api.lastResponse() != null) {
                scenario.attach(api.lastResponse().text(), "application/json", "Last API response");
            }
            api.petsToCleanUp().forEach(api.pets()::deletePet);
        } finally {
            api.request().dispose();
        }
    }

    // ------------------------------------------------------------------- UI

    @Before("@ui")
    public void startUiScenario() {
        BrowserContext context = PlaywrightManager.browser().newContext(new Browser.NewContextOptions()
                .setBaseURL(Config.orangeHrmBaseUrl())
                .setViewportSize(1440, 900));
        context.setDefaultTimeout(Config.defaultTimeoutMillis());
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(false));
        Page page = context.newPage();
        ui.init(context, page);
    }

    @After("@ui")
    public void finishUiScenario(Scenario scenario) {
        BrowserContext context = ui.browserContext();
        try {
            if (scenario.isFailed()) {
                String name = safeFileName(scenario.getName());
                byte[] png = ui.page().screenshot(new Page.ScreenshotOptions()
                        .setFullPage(true)
                        .setPath(Path.of("target", "screenshots", name + ".png")));
                scenario.attach(png, "image/png", "Screenshot at failure");
                context.tracing().stop(new Tracing.StopOptions().setPath(Path.of("target", "traces", name + ".zip")));
                scenario.log("Playwright trace saved: target/traces/" + name + ".zip");
            } else {
                context.tracing().stop(); // discard trace for passing scenarios
            }
        } finally {
            context.close();
        }
    }

    private static String safeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]+", "_");
    }
}

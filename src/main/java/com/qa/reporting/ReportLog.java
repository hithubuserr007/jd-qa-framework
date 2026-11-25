package com.qa.reporting;

/**
 * Step-level logging used by framework code (API client, page objects).
 * Writes to the console, where Cucumber's "pretty" output interleaves it with each step.
 * Framework code depends only on this class, so the reporting tool can be swapped
 * (Allure, Extent, ReportPortal) without touching clients or page objects.
 */
public final class ReportLog {

    private ReportLog() {
    }

    /** Detailed entry, e.g. an HTTP request/response. */
    public static void info(String message) {
        System.out.println("    " + message.replace("\n", "\n    "));
    }

    /** One-line business step, e.g. "Log in as 'Admin'". */
    public static void step(String description) {
        System.out.println("  > " + description);
    }
}

package com.qa.ui.support;

import java.util.regex.Pattern;

/**
 * Builds URL patterns for Playwright's assertThat(page).hasURL(Pattern).
 *
 * Playwright for Java sends the Pattern's source to the browser, where it is evaluated as a
 * JavaScript regular expression. Java-only syntax such as Pattern.quote()'s \Q...\E is not
 * understood by JavaScript, so special characters are escaped individually instead.
 */
public final class UrlPatterns {

    private UrlPatterns() {
    }

    /** Matches any URL that ends with the given path (ignoring the host/base URL). */
    public static Pattern endsWith(String path) {
        return Pattern.compile(".*" + escapeForJs(path) + "$");
    }

    private static String escapeForJs(String literal) {
        return literal.replaceAll("[.*+?^${}()|\\[\\]\\\\/]", "\\\\$0");
    }
}

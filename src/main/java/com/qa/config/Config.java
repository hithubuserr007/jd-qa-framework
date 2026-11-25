package com.qa.config;

/**
 * Single source of truth for runtime configuration.
 *
 * Resolution order for every key:
 *   1. JVM system property   (mvn test -Dorangehrm.password=...)
 *   2. Environment variable  (ORANGEHRM_PASSWORD=...)  - key upper-cased, '.' replaced with '_'
 *   3. Built-in default      (points at the public demo environments, so zero setup is needed)
 *
 * Secrets would never have defaults in a real project; they would come from a vault / CI secret store.
 * The defaults here are the publicly documented demo credentials.
 */
public final class Config {

    private Config() {
    }

    public static String petstoreBaseUrl() {
        return withTrailingSlash(get("petstore.baseUrl", "https://petstore.swagger.io/v2/"));
    }

    public static String orangeHrmBaseUrl() {
        return get("orangehrm.baseUrl", "https://opensource-demo.orangehrmlive.com");
    }

    public static String orangeHrmUsername() {
        return get("orangehrm.username", "Admin");
    }

    public static String orangeHrmPassword() {
        return get("orangehrm.password", "admin123");
    }

    /** chromium | firefox | webkit */
    public static String browser() {
        return get("browser", "chromium");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "true"));
    }

    public static double slowMoMillis() {
        return Double.parseDouble(get("slowMo", "0"));
    }

    public static double defaultTimeoutMillis() {
        return Double.parseDouble(get("timeout", "15000"));
    }

    static String get(String key, String defaultValue) {
        String fromSystem = System.getProperty(key);
        if (fromSystem != null && !fromSystem.isBlank()) {
            return fromSystem;
        }
        String fromEnv = System.getenv(key.toUpperCase().replace('.', '_'));
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return defaultValue;
    }

    private static String withTrailingSlash(String url) {
        return url.endsWith("/") ? url : url + "/";
    }
}

package com.qa.ui.data;

import com.qa.config.Config;

public record UserCredentials(String username, String password) {

    /** Admin user resolved from configuration (system property / env var / demo default). */
    public static UserCredentials admin() {
        return new UserCredentials(Config.orangeHrmUsername(), Config.orangeHrmPassword());
    }

    /** Password is never written to logs or the report. */
    @Override
    public String toString() {
        return "UserCredentials[username=" + username + ", password=****]";
    }
}

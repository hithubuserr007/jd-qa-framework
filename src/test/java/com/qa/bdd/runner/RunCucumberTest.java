package com.qa.bdd.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Entry point for the BDD suite: `mvn clean test` runs every .feature file under
 * src/test/resources/features on the JUnit 5 platform.
 *
 * Filter by tag without code changes:
 *   mvn test -Dcucumber.filter.tags="@api"
 *   mvn test -Dcucumber.filter.tags="@ui and @smoke"
 *   mvn test -Dcucumber.filter.tags="@negative and not @ui"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.qa.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value =
        "pretty, "
        + "summary, "
        + "html:target/cucumber-reports/cucumber.html, "
        + "json:target/cucumber-reports/cucumber.json, "
        + "junit:target/cucumber-reports/cucumber.xml")
public class RunCucumberTest {
}

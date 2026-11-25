package com.qa.bdd.steps;

import com.microsoft.playwright.APIResponse;
import com.qa.api.model.ApiError;
import com.qa.api.model.Pet;
import com.qa.api.model.PetStatus;
import com.qa.api.support.Json;
import com.qa.api.support.Retry;
import com.qa.bdd.context.ApiContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.Duration;

import static com.qa.api.builder.PetBuilder.aPet;
import static com.qa.api.builder.PetBuilder.uniqueId;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the Petstore API features.
 * Steps are thin: they translate Gherkin into calls on the framework's PetApiClient and
 * PetBuilder, and hold the assertions. No URLs, headers or JSON are built here.
 */
public class PetSteps {

    // The public Petstore is eventually consistent across nodes: preconditions poll
    // briefly until the data is visible, instead of using fixed sleeps.
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration POLL_DELAY = Duration.ofSeconds(1);

    private final ApiContext api;

    public PetSteps(ApiContext api) {
        this.api = api;
    }

    // ------------------------------------------------------------------ Given

    @Given("a pet named {string} with status {string} exists in the store")
    public void aPetExistsInTheStore(String name, String status) {
        Pet pet = aPet()
                .withName(name)
                .withStatus(PetStatus.valueOf(status.toUpperCase()))
                .build();

        APIResponse created = api.pets().createPet(pet);
        assertThat(created.status()).as("Precondition: POST /pet should create the pet").isEqualTo(200);
        api.petsToCleanUp().add(pet.id());

        APIResponse visible = waitForStatus(pet.id(), 200);
        assertThat(visible.status()).as("Precondition: the new pet should be retrievable").isEqualTo(200);

        api.setCreatedPet(pet);
    }

    @Given("no pet exists with a newly generated ID")
    public void noPetExistsWithANewlyGeneratedId() {
        api.setPetId(uniqueId());
    }

    // ------------------------------------------------------------------- When

    @When("I request the pet by its ID")
    public void iRequestThePetByItsId() {
        api.setLastResponse(api.pets().getPetById(api.petId()));
    }

    // ------------------------------------------------------------------- Then

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(api.lastResponse().status()).as("HTTP status").isEqualTo(expectedStatus);
    }

    @Then("the response should be in JSON format")
    public void theResponseShouldBeInJsonFormat() {
        assertThat(api.lastResponse().headers().get("content-type")).as("Content-Type").contains("application/json");
    }

    @Then("the returned pet should match the pet that was created")
    public void theReturnedPetShouldMatchThePetThatWasCreated() {
        Pet actual = Json.fromJson(api.lastResponse().text(), Pet.class);
        assertThat(actual)
                .as("Every field returned should match what was created")
                .usingRecursiveComparison()
                .isEqualTo(api.createdPet());
    }

    @Then("the error message should be {string}")
    public void theErrorMessageShouldBe(String expectedMessage) {
        ApiError error = Json.fromJson(api.lastResponse().text(), ApiError.class);
        assertThat(error.type()).as("error.type").isEqualTo("error");
        assertThat(error.message()).as("error.message").isEqualTo(expectedMessage);
    }

    // ---------------------------------------------------------------- helpers

    private APIResponse waitForStatus(long petId, int expectedStatus) {
        return Retry.until(() -> api.pets().getPetById(petId),
                response -> response.status() == expectedStatus,
                MAX_ATTEMPTS, POLL_DELAY);
    }
}

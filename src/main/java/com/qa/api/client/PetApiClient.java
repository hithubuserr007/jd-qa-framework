package com.qa.api.client;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.qa.api.model.Pet;
import com.qa.api.support.Json;
import com.qa.reporting.ReportLog;

/**
 * Service-object for the /pet resource.
 *
 * Tests talk in domain terms (createPet, getPetById) and never build URLs or headers themselves.
 * The client deliberately returns the raw APIResponse rather than throwing on non-2xx, because
 * negative tests need to assert on error status codes and bodies.
 */
public class PetApiClient {

    private static final String PET = "pet";

    private final APIRequestContext request;

    public PetApiClient(APIRequestContext request) {
        this.request = request;
    }

    public APIResponse createPet(Pet pet) {
        String body = Json.toJson(pet);
        APIResponse response = request.post(PET, RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(body));
        log("POST", PET, body, response);
        return response;
    }

    public APIResponse getPetById(long petId) {
        return getPetByRawId(String.valueOf(petId));
    }

    /** Raw-string overload so negative tests can send malformed ids ("abc", "1.5", overflow values). */
    public APIResponse getPetByRawId(String rawPetId) {
        String path = PET + "/" + rawPetId;
        APIResponse response = request.get(path);
        log("GET", path, null, response);
        return response;
    }

    public APIResponse deletePet(long petId) {
        String path = PET + "/" + petId;
        APIResponse response = request.delete(path);
        log("DELETE", path, null, response);
        return response;
    }

    private static void log(String method, String path, String requestBody, APIResponse response) {
        StringBuilder entry = new StringBuilder()
                .append(method).append(' ').append(path)
                .append(" -> ").append(response.status()).append(' ').append(response.statusText());
        if (requestBody != null) {
            entry.append("\nRequest: ").append(requestBody);
        }
        entry.append("\nResponse: ").append(response.text());
        ReportLog.info(entry.toString());
    }
}

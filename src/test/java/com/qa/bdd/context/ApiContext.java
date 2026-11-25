package com.qa.bdd.context;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.qa.api.client.PetApiClient;
import com.qa.api.model.Pet;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-scenario state for API scenarios ("scenario context" / "world").
 * PicoContainer creates a fresh instance for every scenario and injects the same instance
 * into the hooks and every step-definition class that asks for it, so steps can share data
 * (e.g. the pet created in a Given is the one requested in the When) without static fields.
 */
public class ApiContext {

    private APIRequestContext request;
    private PetApiClient pets;
    private Pet createdPet;
    private long petId;
    private APIResponse lastResponse;
    private final List<Long> petsToCleanUp = new ArrayList<>();

    public void init(APIRequestContext request) {
        this.request = request;
        this.pets = new PetApiClient(request);
    }

    public APIRequestContext request() {
        return request;
    }

    public PetApiClient pets() {
        return pets;
    }

    public Pet createdPet() {
        return createdPet;
    }

    public void setCreatedPet(Pet pet) {
        this.createdPet = pet;
        this.petId = pet.id();
    }

    public long petId() {
        return petId;
    }

    public void setPetId(long petId) {
        this.petId = petId;
    }

    public APIResponse lastResponse() {
        return lastResponse;
    }

    public void setLastResponse(APIResponse response) {
        this.lastResponse = response;
    }

    public List<Long> petsToCleanUp() {
        return petsToCleanUp;
    }
}

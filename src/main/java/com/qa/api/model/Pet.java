package com.qa.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Contract model for the Petstore "Pet" definition.
 * Records give us immutability and value equality, so a whole response can be compared
 * against the expected object in one assertion.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Pet(
        Long id,
        Category category,
        String name,
        List<String> photoUrls,
        List<Tag> tags,
        PetStatus status) {
}

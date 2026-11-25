package com.qa.api.builder;

import com.qa.api.model.Category;
import com.qa.api.model.Pet;
import com.qa.api.model.PetStatus;
import com.qa.api.model.Tag;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test Data Builder.
 *  - Builds a valid Pet by default, so each test only states the fields it cares about.
 *  - Generates a unique id per call. The public Petstore is a shared sandbox, so fixed ids
 *    (e.g. 1, 10) are routinely overwritten or deleted by other users and cause flaky tests.
 */
public final class PetBuilder {

    private Long id = uniqueId();
    private Category category = new Category(1L, "Dogs");
    private String name = "qa-auto-pet-" + id;
    private List<String> photoUrls = List.of("https://example.com/photos/dog.png");
    private List<Tag> tags = List.of(new Tag(1L, "automation"));
    private PetStatus status = PetStatus.AVAILABLE;

    private PetBuilder() {
    }

    public static PetBuilder aPet() {
        return new PetBuilder();
    }

    /** Large random id inside the int64 range accepted by Petstore; collision risk is negligible. */
    public static long uniqueId() {
        return ThreadLocalRandom.current().nextLong(9_000_000_000_000L, 9_999_999_999_999L);
    }

    public PetBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public PetBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public PetBuilder withStatus(PetStatus status) {
        this.status = status;
        return this;
    }

    public PetBuilder withCategory(Category category) {
        this.category = category;
        return this;
    }

    public PetBuilder withTags(List<Tag> tags) {
        this.tags = tags;
        return this;
    }

    public Pet build() {
        return new Pet(id, category, name, photoUrls, tags, status);
    }
}

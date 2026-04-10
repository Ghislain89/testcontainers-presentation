package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enriched fruit details combining fruit data with external nutrition info.
 */
public record FruitDetails(
        @JsonProperty("id") Long id,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("nutrition") NutritionInfo nutrition
) {
    public static FruitDetails of(Fruit fruit, NutritionInfo nutrition) {
        return new FruitDetails(fruit.id, fruit.name, fruit.description, nutrition);
    }
}

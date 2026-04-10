package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Domain event emitted when a fruit is created or deleted.
 */
public record FruitEvent(
        @JsonProperty("eventType") String eventType,
        @JsonProperty("fruitId") Long fruitId,
        @JsonProperty("fruitName") String fruitName
) {
    public static final String CREATED = "FRUIT_CREATED";
    public static final String DELETED = "FRUIT_DELETED";

    public static FruitEvent created(Fruit fruit) {
        return new FruitEvent(CREATED, fruit.id, fruit.name);
    }

    public static FruitEvent deleted(Long id, String name) {
        return new FruitEvent(DELETED, id, name);
    }
}

package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Nutrition data returned by the external nutrition API.
 */
public record NutritionInfo(
        @JsonProperty("calories") int calories,
        @JsonProperty("sugar") String sugar,
        @JsonProperty("fiber") String fiber
) {
}

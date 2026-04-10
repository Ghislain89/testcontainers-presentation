package com.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for an external nutrition API.
 * Used to demonstrate WireMock-based testing of external service dependencies.
 */
@Path("/api/nutrition")
@RegisterRestClient(configKey = "nutrition-api")
public interface NutritionClient {

    @GET
    @Path("/{fruitName}")
    NutritionInfo getNutrition(@PathParam("fruitName") String fruitName);
}

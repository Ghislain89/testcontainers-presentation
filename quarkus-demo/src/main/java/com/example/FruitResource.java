package com.example;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.net.URI;
import java.util.List;

@Path("/fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {

    @Inject
    FruitEventProducer eventProducer;

    @Inject
    @RestClient
    NutritionClient nutritionClient;

    @GET
    public List<Fruit> list() {
        return Fruit.listAll();
    }

    @GET
    @Path("/{id}")
    public Fruit get(@PathParam("id") Long id) {
        Fruit fruit = Fruit.findById(id);
        if (fruit == null) {
            throw new NotFoundException("Fruit not found: " + id);
        }
        return fruit;
    }

    /**
     * Returns enriched fruit details including nutrition info from an external API.
     */
    @GET
    @Path("/{id}/details")
    public FruitDetails getDetails(@PathParam("id") Long id) {
        Fruit fruit = Fruit.findById(id);
        if (fruit == null) {
            throw new NotFoundException("Fruit not found: " + id);
        }
        NutritionInfo nutrition = nutritionClient.getNutrition(fruit.name.toLowerCase());
        return FruitDetails.of(fruit, nutrition);
    }

    @POST
    @Transactional
    public Response create(Fruit fruit) {
        fruit.id = null;
        fruit.persist();
        eventProducer.send(FruitEvent.created(fruit));
        return Response.created(URI.create("/fruits/" + fruit.id)).entity(fruit).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        Fruit fruit = Fruit.findById(id);
        if (fruit == null) {
            throw new NotFoundException("Fruit not found: " + id);
        }
        String name = fruit.name;
        fruit.delete();
        eventProducer.send(FruitEvent.deleted(id, name));
    }
}

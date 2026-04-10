package com.example;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {

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

    @POST
    @Transactional
    public Response create(Fruit fruit) {
        fruit.id = null;
        fruit.persist();
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
        fruit.delete();
    }
}

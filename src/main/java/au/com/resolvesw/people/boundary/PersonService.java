package au.com.resolvesw.people.boundary;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import au.com.resolvesw.people.controller.PersonController;
import au.com.resolvesw.people.entity.Person;

/**
 * @author sfcoy
 */
@Stateless
@Path("/people")
public class PersonService {

    @EJB
    private PersonController personController;
    
    @Path("/")
    @POST
    @Consumes(APPLICATION_JSON)
    public Response create(@Valid Person person) {
        personController.persist(person);
        return Response.created(UriBuilder.fromResource(PersonService.class)
                .path(PersonService.class, "getPerson")
                .resolveTemplate("id", person.getId()).build())
                .build();
    }

    @Path("/")
    @GET
    public Response getPerson(String id) {
        return Response.ok(personController.find(id)).build();
    }
    
}

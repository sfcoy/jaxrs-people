package au.com.resolvesw.people.boundary;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import au.com.resolvesw.people.entity.Person;

/**
 * @author sfcoy
 */
@Stateless
@Path("/people")
public class PersonService {

    @PersistenceContext(name="people")
    private EntityManager em;
    
    @POST
    @Path("/")
    @Consumes(APPLICATION_JSON)
    public Response create(@Valid Person person) {
        try {
            em.persist(person);
            return Response.created(UriBuilder.fromResource(PersonService.class)
                        .path(PersonService.class, "getPerson")
                        .resolveTemplate("id", person.getId()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT)
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getPerson(@PathParam("id") String id) {
        return Response.ok(em.find(Person.class, id)).build();
    }
    
}

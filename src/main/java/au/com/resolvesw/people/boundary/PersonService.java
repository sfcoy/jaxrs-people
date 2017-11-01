package au.com.resolvesw.people.boundary;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import au.com.resolvesw.controller.JpaPager;
import au.com.resolvesw.people.entity.Person;
import com.mongodb.DuplicateKeyException;

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
    public Response create(@Valid Person person) throws DuplicateKeyException {
        em.persist(person);
        return Response.created(UriBuilder.fromResource(PersonService.class)
                    .path(PersonService.class, "getPerson")
                    .resolveTemplate("id", person.getId()).build())
                .build();
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Response getPerson(@PathParam("id") String id) {
        final Person foundPerson = em.find(Person.class, id);
        if (foundPerson != null) {
            return Response.ok(foundPerson).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/")
    @Produces(APPLICATION_JSON)
    public Response getPeople(@QueryParam("page") int page, @QueryParam("pageSize") int pageSize) {
        final JpaPager pager = new JpaPager(page, pageSize);
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Person> cq = cb.createQuery(Person.class);
        final Root<Person> rootEntry = cq.from(Person.class);
        final TypedQuery<Person> peopleQuery = em.createQuery(cq.select(rootEntry))
                .setFirstResult(pager.firstResult())
                .setMaxResults(pager.maxResults());
        return Response.ok(new GenericEntity<List<Person>>(peopleQuery.getResultList()){}).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePerson(@PathParam("id") String id) {
        Person toBeRemoved = em.find(Person.class, id);
        if (toBeRemoved != null) {
            em.remove(toBeRemoved);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response updatePerson(@Valid Person person) {
        Person updatedPerson = em.merge(person);
        return Response.accepted(updatedPerson).build();
    }
    
}

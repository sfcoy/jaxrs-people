package au.com.resolvesw.people.boundary;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.com.resolvesw.JAXRSConfiguration;
import au.com.resolvesw.people.controller.ConstraintViolationExceptionMapper;
import au.com.resolvesw.people.entity.Person;


@RunWith(Arquillian.class)
public class PersonServiceTest {

    @Deployment
    public static WebArchive create() {
        try {
            return ShrinkWrap.create(WebArchive.class, "PersonServiceTest.war")
                    .addClasses(JAXRSConfiguration.class, PersonService.class, Person.class,
                            ConstraintViolationExceptionMapper.class)
                    .addAsResource("persistence.xml", "META-INF/persistence.xml")
                    .addAsManifestResource("MANIFEST.MF")
                    .addAsManifestResource("persistence.xml")
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        } finally {
            System.out.println("Created PersonServiceTest.war");
        }
    }

    @Test
    @RunAsClient
    public void shouldCreatePerson(@ArquillianResource URL resource) throws URISyntaxException {
        System.out.println("Running test from " + resource);

        final TestPerson.Builder builder = new TestPerson.Builder()
                .withEmailAddress("fred@bloggs.net")
                .withUsername("fred")
                .withGivenNames("Fred")
                .withFamilyName("Bloggs");
        Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(builder.get(), MediaType.APPLICATION_JSON_TYPE))
                .invoke();
        
        assertThat(response.getStatus(), is(HttpServletResponse.SC_CREATED));
        URI location = response.getLocation();
        assertThat(location.getPath(), matchesPattern(".*/resources/people/[\\p{XDigit}]{24}"));
        assertThat(response.hasEntity(), is(false));

    }

    @Test
    @RunAsClient
    public void shouldFailToCreatePersonWithBlankUserName(@ArquillianResource URL resource) throws URISyntaxException {
        System.out.println("Running test from " + resource);

        final TestPerson.Builder builder = new TestPerson.Builder()
                .withEmailAddress("fred@bloggs.net")
                .withUsername("")
                .withGivenNames("Fred")
                .withFamilyName("Bloggs");
        Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(builder.get(), MediaType.APPLICATION_JSON_TYPE))
                .invoke();

        assertThat(response.getStatus(), is(HttpServletResponse.SC_BAD_REQUEST));
        assertThat(response.hasEntity(), is(true));
        GenericType<List<String>> entityType = new GenericType<List<String>>() {};
        List<String> entities = response.readEntity(entityType);
        assertThat(entities, is(not(empty())));
        assertThat(entities, contains("Person.username may not be empty"));
    }

    @Test
    @RunAsClient
    public void shouldFailToCreatePersonWithInvalidEmail(@ArquillianResource URL resource) throws URISyntaxException {
        System.out.println("Running test from " + resource);

        final TestPerson.Builder builder = new TestPerson.Builder()
                .withEmailAddress("fred\\bloggs.net")
                .withUsername("fred")
                .withGivenNames("Fred")
                .withFamilyName("Bloggs");
        Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(builder.get(), MediaType.APPLICATION_JSON_TYPE))
                .invoke();

        assertThat(response.getStatus(), is(HttpServletResponse.SC_BAD_REQUEST));
        assertThat(response.hasEntity(), is(true));
        GenericType<List<String>> entityType = new GenericType<List<String>>() {};
        List<String> entities = response.readEntity(entityType);
        assertThat(entities, is(not(empty())));
        assertThat(entities, contains("Person.emailAddress not a well-formed email address"));
    }

    private static <T> Matcher<String> matchesPattern(final String expectedPattern) {

        return new TypeSafeMatcher<String>() {

            private final Pattern expected = Pattern.compile(expectedPattern);

            @Override
            public void describeTo(Description description) {
                description.appendText("does not match").appendValue(expectedPattern);
            }

            @Override
            protected boolean matchesSafely(String actual) {
                return actual != null && expected.matcher(actual).matches();
            }
        };
    }
}

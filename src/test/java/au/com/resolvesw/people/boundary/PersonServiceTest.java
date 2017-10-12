package au.com.resolvesw.people.boundary;

import static com.mongodb.client.model.Filters.eq;
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

import org.bson.Document;
import org.bson.types.ObjectId;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.com.resolvesw.JAXRSConfiguration;
import au.com.resolvesw.logging.boundary.LoggerProducer;
import au.com.resolvesw.people.entity.Person;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


@RunWith(Arquillian.class)
public class PersonServiceTest {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection peopleCollection;
    
    @Deployment
    public static WebArchive create() {
        try {
            return ShrinkWrap.create(WebArchive.class, "PersonServiceTest.war")
                    .addClasses(JAXRSConfiguration.class,
                                PersonService.class,
                                Person.class,
                                DuplicateKeyExceptionMapper.class,
                                LoggerProducer.class,
                                ConstraintViolationExceptionMapper.class)
                    .addAsResource("persistence.xml", "META-INF/persistence.xml")
                    .addAsManifestResource("MANIFEST.MF")
                    .addAsManifestResource("persistence.xml")
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        } finally {
            System.out.println("Created PersonServiceTest.war");
        }
    }

    @Before
    public void prepareMongoConnection() {
        mongoClient = new MongoClient();
        mongoDatabase = mongoClient.getDatabase("people");
        peopleCollection = mongoDatabase.getCollection("people");
    }

    @After
    public void closeMongoConnection() {
        peopleCollection.deleteMany(new Document());
        mongoClient.close();
    }

    @Test
    @RunAsClient
    public void shouldCreatePerson(@ArquillianResource URL resource) throws URISyntaxException {
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

        String createdId = location.toString().substring((resource.toString() + "resources/people/").length());

        Document createdPerson = findDocumentWithId(createdId);
        assertThat(createdPerson.get("username"), is("fred"));
        assertThat(createdPerson.get("givenNames"), is("Fred"));
        assertThat(createdPerson.get("familyName"), is("Bloggs"));
        assertThat(createdPerson.get("emailAddress"), is("fred@bloggs.net"));

    }

    @Test
    @RunAsClient
    public void shouldFailToCreatePersonWithBlankUserName(@ArquillianResource URL resource) throws URISyntaxException {
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
    public void shouldFailToCreatePersonWithDuplicateUserName(@ArquillianResource URL resource)
            throws URISyntaxException {
        MongoCollection peopleCollection = mongoDatabase.getCollection("people");
        peopleCollection.insertOne(new TestPerson.Builder()
                .withEmailAddress("fred@jones.net")
                .withUsername("fred")
                .withGivenNames("Fred")
                .withFamilyName("Jones").getDocument());
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

        assertThat(response.getStatus(), is(HttpServletResponse.SC_CONFLICT));
        assertThat(response.hasEntity(), is(true));
        GenericType<String> entityType = new GenericType<String>() {
        };
        String responseEntity = response.readEntity(entityType);
        assertThat(responseEntity, matchesPattern(".*duplicate key.*fred.*"));
    }

    @Test
    @RunAsClient
    public void shouldFailToCreatePersonWithDuplicateEmail(@ArquillianResource URL resource)
            throws URISyntaxException {
        MongoCollection peopleCollection = mongoDatabase.getCollection("people");
        peopleCollection.insertOne(new TestPerson.Builder()
                .withEmailAddress("fred@bloggs.net")
                .withUsername("fred.jones")
                .withGivenNames("Fred")
                .withFamilyName("Jones").getDocument());
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

        assertThat(response.getStatus(), is(HttpServletResponse.SC_CONFLICT));
        assertThat(response.hasEntity(), is(true));
        GenericType<String> entityType = new GenericType<String>() {
        };
        String responseEntity = response.readEntity(entityType);
        assertThat(responseEntity, matchesPattern(".*duplicate key.*fred@bloggs\\.net.*"));
    }

    @Test
    @RunAsClient
    public void shouldFailToCreatePersonWithInvalidEmail(@ArquillianResource URL resource) throws URISyntaxException {
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

    private Document findDocumentWithId(String createdId) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("people");
        return collection.find(eq("_id", new ObjectId(createdId))).first();
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

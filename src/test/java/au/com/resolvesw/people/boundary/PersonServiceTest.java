package au.com.resolvesw.people.boundary;

import static com.mongodb.client.model.Filters.eq;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.com.resolvesw.JAXRSConfiguration;
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
                    .addPackages(true,
                            "au.com.resolvesw.people",
                            "au.com.resolvesw.logging",
                            "au.com.resolvesw.controller")
                    .addClass(JAXRSConfiguration.class)
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

    @AfterClass
    public static void nop() {
    }

    @Test
    @RunAsClient
    public void shouldCreatePerson(@ArquillianResource URL resource) throws URISyntaxException {
        final TestPerson.Builder builder = new TestPerson.Builder()
                .withEmailAddress("fred@bloggs.net")
                .withUsername("fred")
                .withGivenNames("Fred")
                .withFamilyName("Bloggs");
        final Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(builder.get(), MediaType.APPLICATION_JSON_TYPE))
                .invoke();
        
        assertThat(response.getStatus(), is(HttpServletResponse.SC_CREATED));
        final URI location = response.getLocation();
        assertThat(location.getPath(), matchesPattern(".*/resources/people/[\\p{XDigit}]{24}"));
        assertThat(response.hasEntity(), is(false));

        final String createdId = location.toString().substring((resource.toString() + "resources/people/").length());

        final Document createdPerson = findMongoDocumentWithId(createdId);
        assertThat(createdPerson.get("username"), is("fred"));
        assertThat(createdPerson.get("givenNames"), is("Fred"));
        assertThat(createdPerson.get("familyName"), is("Bloggs"));
        assertThat(createdPerson.get("emailAddress"), is("fred@bloggs.net"));

        final Response response2 = ClientBuilder.newClient()
                .target(response.getLocation())
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildGet()
                .invoke();
        
    }

    @Test
    @RunAsClient
    public void shouldFailToCreatePersonWithBlankUserName(@ArquillianResource URL resource) throws URISyntaxException {
        final TestPerson.Builder builder = new TestPerson.Builder()
                .withEmailAddress("fred@bloggs.net")
                .withUsername("")
                .withGivenNames("Fred")
                .withFamilyName("Bloggs");
        final Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(builder.get(), MediaType.APPLICATION_JSON_TYPE))
                .invoke();

        assertThat(response.getStatus(), is(HttpServletResponse.SC_BAD_REQUEST));
        assertThat(response.hasEntity(), is(true));
        final List<ConstraintViolation> constraintViolations = response.readEntity(ConstraintViolation.genericType);
        assertThat(constraintViolations, hasSize(1));
        final ConstraintViolation constraintViolation = constraintViolations.get(0);
        assertThat(constraintViolation.getPropertyName(), endsWith("username"));
        assertThat(constraintViolation.getMessage(), is("may not be empty"));
        assertThat(constraintViolation.getInvalidValue(), is(""));
    }

    @Test
    @RunAsClient
    public void shouldFailToCreatePersonWithBlankNames(@ArquillianResource URL resource) throws URISyntaxException {
        final TestPerson.Builder builder = new TestPerson.Builder()
                .withEmailAddress("fred@bloggs.net")
                .withUsername("fred")
                .withGivenNames("")
                .withFamilyName("");
        final Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(builder.get(), MediaType.APPLICATION_JSON_TYPE))
                .invoke();

        assertThat(response.getStatus(), is(HttpServletResponse.SC_BAD_REQUEST));
        assertThat(response.hasEntity(), is(true));
        final List<ConstraintViolation> constraintViolations = response.readEntity(ConstraintViolation.genericType);
        assertThat(constraintViolations, is(not(empty())));
        final ConstraintViolation constraintViolation = constraintViolations.get(0);
        assertThat(constraintViolation.getPropertyName(), anyOf(endsWith("familyName"), endsWith("givenNames")));
        assertThat(constraintViolation.getMessage(), is("may not be empty"));
        assertThat(constraintViolation.getInvalidValue(), is(""));
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
        final Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(builder.get(), MediaType.APPLICATION_JSON_TYPE))
                .invoke();

        assertThat(response.getStatus(), is(HttpServletResponse.SC_CONFLICT));
        assertThat(response.hasEntity(), is(true));
        final GenericType<String> entityType = new GenericType<String>() {};
        final String responseEntity = response.readEntity(entityType);
        assertThat(responseEntity, matchesPattern(".*duplicate key.*fred.*"));
    }

    @Test
    @RunAsClient
    public void shouldFailToCreatePersonWithDuplicateEmail(@ArquillianResource URL resource)
            throws URISyntaxException {
        final MongoCollection peopleCollection = mongoDatabase.getCollection("people");
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
        final Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(builder.get(), MediaType.APPLICATION_JSON_TYPE))
                .invoke();

        assertThat(response.getStatus(), is(HttpServletResponse.SC_CONFLICT));
        assertThat(response.hasEntity(), is(true));
        final GenericType<String> entityType = new GenericType<String>() {};
        final String responseEntity = response.readEntity(entityType);
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
        final Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(builder.get(), MediaType.APPLICATION_JSON_TYPE))
                .invoke();

        assertThat(response.getStatus(), is(HttpServletResponse.SC_BAD_REQUEST));
        assertThat(response.hasEntity(), is(true));
        final List<ConstraintViolation> constraintViolations = response.readEntity(ConstraintViolation.genericType);
        assertThat(constraintViolations, is(not(empty())));
        final ConstraintViolation constraintViolation = constraintViolations.get(0);
        assertThat(constraintViolation.getPropertyName(), endsWith("emailAddress"));
        assertThat(constraintViolation.getMessage(), is("not a well-formed email address"));
        assertThat(constraintViolation.getInvalidValue(), is("fred\\bloggs.net"));
    }

    @Test
    @RunAsClient
    public void shouldFindPersonById(@ArquillianResource URL resource) throws URISyntaxException {
        final MongoCollection peopleCollection = mongoDatabase.getCollection("people");
        final Document testPersonDocument = new TestPerson.Builder()
                .withEmailAddress("fred@bloggs.net")
                .withUsername("fred.jones")
                .withGivenNames("Fred")
                .withFamilyName("Jones").getDocument();
        peopleCollection.insertOne(testPersonDocument);

        final Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .path(testPersonDocument.getObjectId("_id").toString())
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildGet()
                .invoke();

        assertThat(response.getStatus(), is(HttpServletResponse.SC_OK));
        assertThat(response.hasEntity(), is(true));

        final TestPerson foundPerson = response.readEntity(TestPerson.class);
        assertThat(foundPerson.id, is(testPersonDocument.getObjectId("_id").toString()));
        assertThat(foundPerson.username, is(testPersonDocument.get("username")));
        assertThat(foundPerson.emailAddress, is(testPersonDocument.get("emailAddress")));
        assertThat(foundPerson.givenNames, is(testPersonDocument.get("givenNames")));
        assertThat(foundPerson.familyName, is(testPersonDocument.get("familyName")));
    }

    @Test
    @RunAsClient
    public void shouldFindFirstPageOfPeople(@ArquillianResource URL resource) throws URISyntaxException, IOException {
        loadTestData("au-500.csv");
        final Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .queryParam("page", 1)
                .queryParam("pageSize", 25)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildGet()
                .invoke();

        assertThat(response.getStatus(), is(HttpServletResponse.SC_OK));
        assertThat(response.hasEntity(), is(true));

        final List<TestPerson> foundPageOfPeople = response.readEntity(new GenericType<List<TestPerson>>() {});
        assertThat(foundPageOfPeople, is(iterableWithSize(25)));

        final TestPerson firstPersonFound = foundPageOfPeople.get(0);
        assertThat(firstPersonFound.username, is("rebbecca.didio"));
        assertThat(firstPersonFound.emailAddress, is("rebbecca.didio@didio.com.au"));
        assertThat(firstPersonFound.givenNames, is("Rebbecca"));
        assertThat(firstPersonFound.familyName, is("Didio"));

        final TestPerson lastPersonFound = foundPageOfPeople.get(24);
        assertThat(lastPersonFound.username, is("barrie.nicley"));
        assertThat(lastPersonFound.emailAddress, is("bnicley@nicley.com.au"));
        assertThat(lastPersonFound.givenNames, is("Barrie"));
        assertThat(lastPersonFound.familyName, is("Nicley"));
    }

    @Test
    @RunAsClient
    public void shouldFindLastPageOfPeople(@ArquillianResource URL resource) throws URISyntaxException, IOException {
        loadTestData("au-500.csv");
        final Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .queryParam("page", 20)
                .queryParam("pageSize", 25)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildGet()
                .invoke();

        assertThat(response.getStatus(), is(HttpServletResponse.SC_OK));
        assertThat(response.hasEntity(), is(true));

        final List<TestPerson> foundPageOfPeople = response.readEntity(new GenericType<List<TestPerson>>() {});
        assertThat(foundPageOfPeople, is(iterableWithSize(25)));

        final TestPerson firstPersonFound = foundPageOfPeople.get(0);
        assertThat(firstPersonFound.username, is("onita.milbrandt"));
        assertThat(firstPersonFound.emailAddress, is("onita.milbrandt@milbrandt.com.au"));
        assertThat(firstPersonFound.givenNames, is("Onita"));
        assertThat(firstPersonFound.familyName, is("Milbrandt"));

        final TestPerson lastPersonFound = foundPageOfPeople.get(24);
        assertThat(lastPersonFound.username, is("lenora.delacruz"));
        assertThat(lastPersonFound.emailAddress, is("lenora@delacruz.net.au"));
        assertThat(lastPersonFound.givenNames, is("Lenora"));
        assertThat(lastPersonFound.familyName, is("Delacruz"));
    }

    private Document findMongoDocumentWithId(String createdId) {
        final MongoCollection<Document> collection = mongoDatabase.getCollection("people");
        return collection.find(eq("_id", new ObjectId(createdId))).first();
    }

    private void loadTestData(String testFileName) throws IOException {
        final File testDataDir = new File("src/test/data");
        final File testDataFile = new File(testDataDir, testFileName);
        for (CSVRecord csvRecord: CSVParser.parse(testDataFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            final TestPerson.Builder builder = new TestPerson.Builder()
                    .withEmailAddress(csvRecord.get("email"))
                    .withUsername((csvRecord.get("first_name") + "." + csvRecord.get("last_name")).toLowerCase())
                    .withGivenNames(csvRecord.get("first_name"))
                    .withFamilyName(csvRecord.get("last_name"));
            peopleCollection.insertOne(builder.getDocument());
        }
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

    private static class ConstraintViolation {
        private String propertyName;
        private String message;
        private String invalidValue;

        final static GenericType<List<ConstraintViolation>> genericType
                = new GenericType<List<ConstraintViolation>>() {};

        public ConstraintViolation() {
        }

        String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        String getInvalidValue() {
            return invalidValue;
        }

        public void setInvalidValue(String invalidValue) {
            this.invalidValue = invalidValue;
        }
    }

}

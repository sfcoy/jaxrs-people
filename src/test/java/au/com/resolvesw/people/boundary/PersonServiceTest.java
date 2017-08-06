package au.com.resolvesw.people.boundary;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.com.resolvesw.JAXRSConfiguration;
import au.com.resolvesw.people.entity.Person;


@RunWith(Arquillian.class)
public class PersonServiceTest {

    @Deployment(testable=false)
    public static WebArchive create() {
        try {
            return ShrinkWrap.create(WebArchive.class, "PersonServiceTest.war")
                    .addClasses(JAXRSConfiguration.class, PersonService.class, Person.class)
                    .addAsManifestResource("META-INF/persistence.xml");
        } finally {
            System.out.println("Created PersonServiceTest.war");
        }
    }

    @XmlRootElement
    public static class Persona {

        private String emailAddress;

        private String username;

        private String familyName;

        private String givenNames;

        @XmlElement
        public String getEmailAddress() {
            return emailAddress;
        }

        @XmlElement
        public String getUsername() {
            return username;
        }

        @XmlElement
        public String getFamilyName() {
            return familyName;
        }

        @XmlElement
        public String getGivenNames() {
            return givenNames;
        }
    }

    private static class Builder {

        Persona persona = new Persona();

        Builder withEmailAddress(String emailAddress) {
            persona.emailAddress = emailAddress;
            return this;
        }

        Builder withUsername(String username) {
            persona.username = username;
            return this;
        }

        Builder withFamilyName(String familyName) {
            persona.familyName = familyName;
            return this;
        }

        Builder withGivenNames(String givenNames) {
            persona.givenNames = givenNames;
            return this;
        }

        Persona get() {
            return persona;
        }
        
    }
    @Test
    @RunAsClient
    public void shouldCreatePerson(@ArquillianResource URL resource) throws URISyntaxException {
        System.out.println("Running test from " + resource);

        final Builder builder = new Builder()
                .withEmailAddress("fred@bloggs.net")
                .withUsername("fred")
                .withGivenNames("Fred")
                .withFamilyName("Bloggs");
        Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/people/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(builder.get(), MediaType
                        .APPLICATION_JSON_TYPE)).invoke();
        System.out.println(response.readEntity(String.class));
        assertThat(response.getStatus(), is(200));
    }
}

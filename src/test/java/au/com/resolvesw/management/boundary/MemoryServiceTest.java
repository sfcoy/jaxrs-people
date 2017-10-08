package au.com.resolvesw.management.boundary;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.client.ClientBuilder;
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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.com.resolvesw.JAXRSConfiguration;

@RunWith(Arquillian.class)
public class MemoryServiceTest {

    @Deployment
    public static WebArchive create() {
        try {
            return ShrinkWrap.create(WebArchive.class, "MemoryServiceTest.war")
                    .addClasses(JAXRSConfiguration.class, MemoryService.class);
        } finally {
            System.out.println("Created MemoryServiceTest.war");
        }
    }

    @Test
    @RunAsClient
    public void shouldGetHeapMemoryUsage(@ArquillianResource URL resource) throws URISyntaxException, IOException {
        Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/memory/heapMemoryUsage")
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildGet()
                .invoke();
        assertThat(response.getStatus(), is(200));
        assertThat(response.hasEntity(), is(true));
        String responseJson = response.readEntity(String.class);
        JsonReader jsonReader = Json.createReader(new StringReader(responseJson));
        JsonObject jsonNode = jsonReader.readObject();
        assertThat(jsonNode.get("init"), is(integerValue()));
        assertThat(jsonNode.get("used"), is(integerValue()));
        assertThat(jsonNode.get("committed"), is(integerValue()));
        assertThat(jsonNode.get("max"), is(integerValue()));
    }

    @Test
    @RunAsClient
    public void shouldGetNonHeapMemoryUsage(@ArquillianResource URL resource) throws URISyntaxException, IOException {
        Response response = ClientBuilder.newClient()
                .target(resource.toURI())
                .path("resources/memory/nonHeapMemoryUsage")
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildGet()
                .invoke();
        assertThat(response.getStatus(), is(200));
        assertThat(response.hasEntity(), is(true));
        String responseJson = response.readEntity(String.class);
        JsonReader jsonReader = Json.createReader(new StringReader(responseJson));
        JsonObject jsonNode = jsonReader.readObject();
        assertThat(jsonNode.get("init"), is(integerValue()));
        assertThat(jsonNode.get("used"), is(integerValue()));
        assertThat(jsonNode.get("committed"), is(integerValue()));
        assertThat(jsonNode.get("max"), is(integerValue()));
    }

    private static <T> Matcher<JsonValue> integerValue() {
        return new TypeSafeMatcher<JsonValue>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("is not numeric");
            }

            @Override
            protected boolean matchesSafely(JsonValue node) {
                if (node != null) {
                    try {
                        return node.getValueType() == JsonValue.ValueType.NUMBER;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        };
    }
}

package au.com.resolvesw.system.boundary;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hamcrest.Description;
import org.hamcrest.Factory;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(Arquillian.class)
public class MemoryServiceTest {

    @Deployment(testable=false)
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
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(new StringReader(responseJson));
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
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(new StringReader(responseJson));
        assertThat(jsonNode.get("init"), is(integerValue()));
        assertThat(jsonNode.get("used"), is(integerValue()));
        assertThat(jsonNode.get("committed"), is(integerValue()));
        assertThat(jsonNode.get("max"), is(integerValue()));
    }

    @Factory
    private static <T> Matcher<JsonNode> integerValue() {
        return new TypeSafeMatcher<JsonNode>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("is not numeric");
            }

            @Override
            protected boolean matchesSafely(JsonNode node) {
                if (node != null) {
                    try {
                        Integer.valueOf(node.asText());
                        return true;
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

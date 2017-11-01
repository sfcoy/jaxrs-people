package au.com.resolvesw.people.boundary;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.JsonWriter;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import au.com.resolvesw.people.entity.Person;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class PersonBodyWriter implements MessageBodyWriter<Person>{

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Person.class.equals(type) && MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
    }

    @Override
    public long getSize(Person person, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Person person, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        try (final JsonWriter jsonWriter = Json.createWriter(entityStream)) {
            jsonWriter.writeObject(person.toJson());
        }
    }
}

package au.com.resolvesw.people.boundary;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import au.com.resolvesw.people.entity.Person;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class PersonListBodyWriter implements MessageBodyWriter<List<Person>> {

    private static final GenericEntity<List<Person>> GENERIC_PERSON_LIST_ENTITY
            = new GenericEntity<List<Person>>(Collections.emptyList()){};

    @Inject
    private Logger logger;
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return List.class.isAssignableFrom(type) && GENERIC_PERSON_LIST_ENTITY.getType().equals(genericType)
                && MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
    }

    @Override
    public long getSize(List<Person> people, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(List<Person> people, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try (final JsonWriter jsonWriter = Json.createWriter(entityStream)) {
            JsonArrayBuilder peopleArrayBuilder = Json.createArrayBuilder();
            people.forEach(
                    person -> peopleArrayBuilder.add(person.toJson())
            );
            jsonWriter.writeArray(peopleArrayBuilder.build());
        }
    }
}

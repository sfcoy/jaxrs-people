package au.com.resolvesw.people.boundary;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.bson.BsonValue;

import com.mongodb.DuplicateKeyException;

@Provider
public class DuplicateKeyExceptionMapper implements ExceptionMapper<DuplicateKeyException>{
    
    @Inject
    private Logger logger;
    
    @Override
    public Response toResponse(DuplicateKeyException exception) {
        final BsonValue mongoResponse = exception.getResponse().get("err");
        logger.log(Level.FINE, "{0}", mongoResponse);
        return Response.status(Response.Status.CONFLICT)
                .entity(mongoResponse)
                .build();
    }
    
}

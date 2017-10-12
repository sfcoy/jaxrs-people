package au.com.resolvesw.people.boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException>{

    @Inject
    private Logger logger;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
//        logger.log(Level.WARNING, "Constraint violation: {}", exception.getMessage());
        List<String> messages = new ArrayList<>();
        for (ConstraintViolation cv : exception.getConstraintViolations()) {
            String propertyName = "";
            for (Path.Node node: cv.getPropertyPath()) {
                propertyName = node.getName();
            }
            messages.add(cv.getLeafBean().getClass().getSimpleName() + "." + propertyName + " " + cv.getMessage());
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(messages)
                .build();
    }
    
}

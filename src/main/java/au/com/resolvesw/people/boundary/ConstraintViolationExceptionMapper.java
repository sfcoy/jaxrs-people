package au.com.resolvesw.people.boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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

    private static class ConstraintViolationBean {
        private final String propertyName;
        private final String message;
        private final String invalidValue;

        private ConstraintViolationBean(ConstraintViolation constraintViolation) {
            final StringBuilder propertyPath = new StringBuilder();
            for (Path.Node node: constraintViolation.getPropertyPath()) {
                if (propertyPath.length() > 0) {
                    propertyPath.append('.');
                }
                propertyPath.append(node.getName());
            }
            this.propertyName = propertyPath.toString();
            this.message = constraintViolation.getMessage();
            this.invalidValue = constraintViolation.getInvalidValue().toString();
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getMessage() {
            return message;
        }

        public String getInvalidValue() {
            return invalidValue;
        }
    }
    
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        logger.log(Level.WARNING, "Constraint violation: {}", exception.getMessage());
        List<ConstraintViolationBean> messages = new ArrayList<>();
        for (ConstraintViolation cv : exception.getConstraintViolations()) {
            messages.add(new ConstraintViolationBean(cv));
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(messages)
                .build();
    }
    
}

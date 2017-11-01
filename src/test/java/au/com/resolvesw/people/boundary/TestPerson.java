package au.com.resolvesw.people.boundary;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.bson.Document;

/**
 * @author sfcoy
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TestPerson {

    // Lifted from org.hibernate.ogm.type.descriptor.impl.CalendarTimeZoneDateTimeTypeDescriptor
    private static final String DATE_TIME_TIMEZONE_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS Z";

    String id;
    
    String emailAddress;

    String username;

    String familyName;

    String givenNames;

    String startDate;
    
    /**
     * @author sfcoy
     */
    static class Builder {

        final TestPerson persona = new TestPerson();

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

        TestPerson get() {
            return persona;
        }

        Document getDocument() {
            return new Document()
                    .append("emailAddress", persona.emailAddress)
                    .append("familyName", persona.familyName)
                    .append("givenNames", persona.givenNames)
                    .append("username", persona.username)
                    .append("startDate", createDateTimeTimeZoneFormat().format(Calendar.getInstance().getTime()));
        }
    }
    
    private static SimpleDateFormat createDateTimeTimeZoneFormat() {
        SimpleDateFormat dateTimeTimeZoneFormat = new SimpleDateFormat( DATE_TIME_TIMEZONE_FORMAT );
        dateTimeTimeZoneFormat.setLenient( false );
        return dateTimeTimeZoneFormat;
    }

}

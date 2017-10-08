package au.com.resolvesw.people.boundary;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author sfcoy
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TestPerson {

    private String emailAddress;

    private String username;

    private String familyName;

    private String givenNames;
    
    /**
     * @author sfcoy
     */
    static class Builder {

        TestPerson persona = new TestPerson();

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

    }
}

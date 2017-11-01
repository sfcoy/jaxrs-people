package au.com.resolvesw.people.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "people")
public class Person implements Serializable {

    private static final long serialVersionUID = 6946537000698428498L;

    // Lifted from org.hibernate.ogm.type.descriptor.impl.CalendarTimeZoneDateTimeTypeDescriptor
    private static final String ISO8601_DATE_TIME_TIMEZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss:SSSz";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Type(type = "objectid")
    private String id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @NotBlank
    private String familyName;

    @NotNull
    private String givenNames;

    @NotNull
    @Email(regexp = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", flags = Pattern.Flag.CASE_INSENSITIVE)
    @Column(unique = true)
    private String emailAddress;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startDate;

    public Person(String username) {
        this.username = username;
    }

    protected Person() {
    }

    public String getId() {
        return id;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenNames() {
        return givenNames;
    }

    public void setGivenNames(String givenNames) {
        this.givenNames = givenNames;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @PrePersist
    void onPrePersist() {
        startDate = Calendar.getInstance();
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", id)
                .add("username", username)
                .add("familyName", familyName)
                .add("givenNames", givenNames)
                .add("emailAddress", emailAddress)
                .add("startDate", createDateTimeTimeZoneFormat().format(startDate.getTime()))
                .build();
    }

    @Override
    public String toString() {
        return "Person{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", familyName='" + familyName + '\'' +
                ", givenNames='" + givenNames + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", startDate=" + startDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(username, person.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    static SimpleDateFormat createDateTimeTimeZoneFormat() {
        SimpleDateFormat dateTimeTimeZoneFormat = new SimpleDateFormat(ISO8601_DATE_TIME_TIMEZONE_FORMAT);
        dateTimeTimeZoneFormat.setLenient( false );
        return dateTimeTimeZoneFormat;
    }

}

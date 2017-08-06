package au.com.resolvesw.people.controller;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import au.com.resolvesw.people.entity.Person;

/**
 * @author sfcoy
 */
@Stateless
public class PersonController {

    @PersistenceContext
    private EntityManager em;

    public void persist(Person person) {
        em.persist(person);
    }

    public Person find(String id) {
        return em.find(Person.class, id);
    }
    
}

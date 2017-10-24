JAX-RS Web Service Example with Hibernate OGM and Arqillian Cube Unit Tests
===

This project contains the implementation of a couple of JAX-RS web services:

 * Memory Service
   Returns the result of querying `java.lang.management.MemoryMXBean`
   
 * Person Service
   Demonstrates CRUD operations on an arbitrary `Person` object that is stored
   in a Mongo Database.   
   This database is accessed using [Hibernate OGM](http://hibernate.org/ogm/).

The deployment platform for all this is a WildFly 10.1 server that has integrated 
Hibernate OGM functionality.

Unit tests are built using [Arquillian Cube](http://arquillian.org/arquillian-cube/)
and exploits the [WildFly with Hibernate OGM + Search](https://hub.docker.com/r/sfcoy/wildfly-ogm/)
docker image.

    docker pull sfcoy/wildfly-ogm

Arquillian Cube starts up MongoDB and WildFly OGM in separate Docker processes and the test
classes are deployed for client side testing.

And the cool thing is **the final product "people.war" is only 14k in size** and contains no WEB-INF/lib jar files.

Notes
--

This implementation is *not Java EE portable* due to:

 * the use of Hibernate OGM to store data in Mongo DB
 * the use of [Hibernate Validator](http://hibernate.org/validator/), which ships with WildFly.


 

 
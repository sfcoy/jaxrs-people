JAX-RS Web Service Example with Hibernate OGM and Arqillian Cube Unit Tests
===

This project contains the implementation of a couple of JAX-RS web services:

 * Memory Service
 
   Returns the result of querying `java.lang.management.MemoryMXBean`
   
 * Person Service
 
   Demonstrates CRUD operations on an arbitrary `Person` object that is stored
   in a Mongo Database.   
   This database is accessed using [Hibernate OGM](http://hibernate.org/ogm/).
   Other features of interest include  
   * Use of [javax.ws.rs.ext.ExceptionMapper](http://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/ExceptionMapper.html)
   * Use of [javax.ws.rs.ext.MessageBodyWriter](http://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/MessageBodyWriter.html)
   

The deployment platform is WildFly 10.1.0.Final that has integrated Hibernate OGM functionality.

Unit tests are built using [Arquillian Cube](http://arquillian.org/arquillian-cube/)
and exploits the [WildFly with Hibernate OGM + Search](https://hub.docker.com/r/sfcoy/wildfly-ogm/)
docker image. The [docker-maven-plugin](https://github.com/spotify/docker-maven-plugin) is used to extend this image
with the project Dockerfile. 

Arquillian Cube starts up MongoDB and WildFly OGM in separate Docker processes and the test
classes are deployed for client side testing.

And the cool thing is **the final product "people.war" is only 24k in size** and contains no WEB-INF/lib jar files.

Notes
--

1. This implementation is *not Java EE portable* due to:
   * the use of Hibernate OGM to store data in Mongo DB
   * the use of [Hibernate Validator](http://hibernate.org/validator/), which ships with WildFly.

2. The unit tests make use of free sample data from [www.briandunning.com/sample-data/](https://www.briandunning.com/sample-data/)

3. The unit tests inherit the JAX-RS Client that is used by [Arquillian Cube](http://arquillian.org/arquillian-cube/)'s
   dependency on the [docker-java](https://github.com/docker-java/docker-java) library.


 

 
FROM sfcoy/wildfly-ogm:latest

ENV ADMIN_USER admin
ENV ADMIN_PASSWORD Admin#70365

RUN bin/add-user.sh $ADMIN_USER $ADMIN_PASSWORD --silent

ENV JAVA_OPTS "-Xms64m -Xmx1024m -Djava.net.preferIPv4Stack=true -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"
EXPOSE 8787

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

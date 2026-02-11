FROM tomcat:10-jre17-temurin

ENV PATH="/usr/local/bin/liquibase:${PATH}" 

COPY docker/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
COPY ./waltz-schema/src/main/resources/liquibase/*.xml /opt/waltz/liquibase/
#COPY ./waltz-web/target/waltz-web.war /usr/local/tomcat/webapps/ROOT.war
COPY ./waltz-web/target/waltz-web-jakarta.war /usr/local/tomcat/webapps/ROOT.war
COPY docker/waltz.properties /home/waltz/.waltz/waltz-template
COPY waltz-web/src/main/resources/logback.example.xml /home/waltz/.waltz/waltz-logback.xml

RUN useradd -ms /bin/bash waltz && \
  mkdir -p /opt/waltz/liquibase /opt/liquibase && \
  chown -R waltz:waltz /usr/local/tomcat /opt/waltz/liquibase /home/waltz/.waltz /opt/liquibase /home/waltz/.waltz/waltz-template && \
  curl -sLO https://github.com/liquibase/liquibase-package-manager/releases/download/v0.1.2/lpm-0.1.2-linux.zip && \
  curl -sLO https://github.com/liquibase/liquibase/releases/download/v4.5.0/liquibase-4.5.0.zip && \
  apt-get update && apt-get install -y unzip postgresql-client gettext-base && \
  unzip -qo lpm-0.1.2-linux.zip -d /usr/local/bin && \
  unzip -qo liquibase-4.5.0.zip -d /opt/liquibase && \
  ln -s  /opt/liquibase/liquibase /usr/local/bin/liquibase && \
  rm -rf /var/lib/apt/lists/* lpm-0.1.2-linux.zip liquibase-4.5.0.zip && \
  lpm update && lpm add -g postgresql

RUN apt-get update && apt-get install -y dos2unix
RUN dos2unix /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080

USER waltz

ENTRYPOINT [ "docker-entrypoint.sh" ]
CMD [ "update",  "run" ]
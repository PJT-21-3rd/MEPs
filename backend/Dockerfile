FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle war --no-daemon -x test

FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/build/libs/*.war /usr/local/tomcat/webapps/ROOT.war
COPY log4j2-deploy.xml /usr/local/tomcat/conf/log4j2-deploy.xml
EXPOSE 8080
ENV CATALINA_OPTS="-Xms256m -Xmx512m -Dlog4j2.configurationFile=/usr/local/tomcat/conf/log4j2-deploy.xml"
CMD ["catalina.sh", "run"]
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle war --no-daemon -x test

FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/build/libs/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
ENV CATALINA_OPTS="-Xms256m -Xmx512m"
CMD ["catalina.sh", "run"]
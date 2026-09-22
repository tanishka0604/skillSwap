# Stage 1: build the WAR with Maven (no Java/Maven needed on your machine)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package

# Stage 2: run it on Tomcat 9 (javax.servlet, matching the pom)
FROM tomcat:9.0-jdk17
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/SkillSwap.war /usr/local/tomcat/webapps/SkillSwap.war
EXPOSE 8080
CMD ["catalina.sh", "run"]

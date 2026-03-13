# Expects target/lutra.war to be pre-built before running docker build.
# Build the WAR first: mvn -B package -DskipTests -pl lutra-webapp -am

# ---------- Stage 1: Extract Tomcat binaries ----------
FROM tomcat:9.0-jdk21-temurin-jammy AS tomcat

# ---------- Stage 2: Minimal JRE runtime ----------
FROM eclipse-temurin:21-jre-jammy

COPY --from=tomcat /usr/local/tomcat /usr/local/tomcat

RUN rm -rf /usr/local/tomcat/webapps/*

COPY target/lutra.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]

# Pull base image
From tomcat:9-jdk11-corretto

LABEL maintainer="m.g.skjaeveland@gmail.com"

RUN chmod -R 777 /usr/local/tomcat/webapps

ADD target/lutra.war /usr/local/tomcat/webapps/


### Build with:
# docker build -t weblutra .
### Run with
# docker run -it --rm -p 8080:8080 --name lutra weblutra
### Open browser:
# http://localhost:8080/lutra

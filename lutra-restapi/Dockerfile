# Pull base image
From tomcat:8-jre8

MAINTAINER "Martin G. Skjæveland <m.g.skjaeveland@gmail.com>"

ADD target/lutra.war /usr/local/tomcat/webapps/


### Build with:
# docker build -t weblutra .
### Run with
# docker run -it --rm -p 8080:8080 --name lutra weblutra
### Open browser:
# http://localhost:8080/lutra

FROM openjdk:8
LABEL maintainer="wkazxf@ajou.ac.kr"
VOLUME /tmp
EXPOSE :8080
ARG JARFILE=/build/libs/entrip-api-kotlin-0.0.1-SNAPSHOT.jar
ADD ${JARFILE} 2ntrip-api-kotlin.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/2ntrip-api-kotlin.jar"]
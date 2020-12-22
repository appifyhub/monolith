FROM azul/zulu-openjdk-alpine:11-jre

ADD build/libs/monolith.jar service.jar
EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "/service.jar" ]
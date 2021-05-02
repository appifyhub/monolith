FROM azul/zulu-openjdk-alpine:11-jre

COPY . /app
WORKDIR /app

# hack to not cache conditional build checks
ADD "https://www.random.org/cgi-bin/randbyte?nbytes=10&format=h" skipcache
RUN sh /app/scripts/conditional_build.sh "/app/build/libs/monolith.jar" && \
  cp /app/build/libs/monolith.jar /service.jar && \
  rm -rf /app /root/.gradle /root/.kotlin

WORKDIR /

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "/service.jar" ]

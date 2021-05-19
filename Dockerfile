FROM azul/zulu-openjdk-alpine:11-jre

COPY . /app
WORKDIR /app

# hack to avoid caching on conditional build checks
ADD "https://www.random.org/cgi-bin/randbyte?nbytes=10&format=h" skipcache

RUN echo "[Conditionally] starting the build JAR..." && \
  sh /app/scripts/conditional_build.sh "/app/build/libs/monolith.jar" && \
  echo "Copying the service JAR..." && \
  cp /app/build/libs/monolith.jar /service.jar && \
  echo "Creating the external IP2Location directory..." && \
  mkdir /ip2location && \
  echo "Copying the IP2Location file..." && \
  mv /app/src/main/resources/ip2location/IP2Location.bin /ip2location/IP2Location.bin && \
  echo "Removing unnecessary leftovers..." && \
  rm -rf /app && \
  rm -rf /root/.gradle && \
  rm -rf /root/.kotlin && \
  echo "Done."

WORKDIR /

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "/service.jar" ]

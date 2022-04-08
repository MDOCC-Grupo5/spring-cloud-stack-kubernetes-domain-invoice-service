ARG JDK_VERSION=11
FROM gcr.io/distroless/java:${JDK_VERSION}-nonroot

USER nonroot:nonroot

WORKDIR /application

COPY --chown=nonroot:nonroot ./target/dependencies/ ./
COPY --chown=nonroot:nonroot ./target/snapshot-dependencies/ ./
COPY --chown=nonroot:nonroot ./target/spring-boot-loader/ ./
COPY --chown=nonroot:nonroot ./target/application/ ./

ENV SPRING_APP_PROFILE=prod
ENV PORT=8080

ENV _JAVA_OPTIONS "-XX:MinRAMPercentage=60.0 -XX:MaxRAMPercentage=90.0 \
-Djava.security.egd=file:/dev/./urandom \
-Djava.awt.headless=true -Dfile.encoding=UTF-8 \
-Dspring.output.ansi.enabled=ALWAYS \
-Dspring.profiles.active=${SPRING_APP_PROFILE}"

EXPOSE ${PORT}

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
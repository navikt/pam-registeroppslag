FROM gcr.io/distroless/java21

COPY build/libs/pam-registeroppslag-all.jar ./app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:-OmitStackTraceInFastThrow -Xms256m -Xmx2304m"

ENTRYPOINT ["java", "-jar", "/app.jar"]
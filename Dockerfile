FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:9eec756379ed5f12ebade505e5b7ecc26e2eb55b46c137c0c47aad8a1757913c

COPY build/libs/pam-registeroppslag-all.jar /app.jar
EXPOSE 8080
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-XX:-OmitStackTraceInFastThrow -XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"

CMD ["-jar", "/app.jar"]

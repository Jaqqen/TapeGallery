# syntax=docker/dockerfile:1

# --- build -------------------------------------------------------------------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY src/main src/main

ARG bootJarFilename=bootJar-filename.txt
# bootJar writes its own archive name into build/${bootJarFilename} (see build.gradle), which is
# what saves the steps below from globbing.
# The mount=type=cache keeps dependencies and the Gradle distribution for the next build.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew wrapper --distribution-type bin && \
    ./gradlew --no-daemon -PdockerfileBootJarFilename=${bootJarFilename} bootJar

# 'jarmode=tools' with '--layers' makes it easier to navigate to the application jar file
# without using wildcards since it's directly inside the 'application' directory.
RUN app="build/libs/$(cat build/${bootJarFilename})" && \
    java -Djarmode=tools -jar "$app" extract --layers --destination extracted && \
    mv extracted/application/*.jar extracted/application/app.jar

# --- runtime -----------------------------------------------------------------
FROM eclipse-temurin:25-jre AS runtime

# declare '/app' as home dir and avoid default home dir creation by using '--no-create-home'
RUN groupadd --system --gid 1001 tapeshub && \
    useradd --system --uid 1001 --gid tapeshub --home-dir /app --no-create-home tapeshub
WORKDIR /app

# Ordered least to most volatile, so a source-only change reuses the dependency layer.
COPY --from=build --chown=tapeshub:tapeshub /workspace/extracted/dependencies/ ./
COPY --from=build --chown=tapeshub:tapeshub /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=tapeshub:tapeshub /workspace/extracted/application/ ./

USER tapeshub
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=80.0", "-jar", "app.jar"]

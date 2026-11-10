FROM gradle:8.14.4-jdk21-alpine AS build
WORKDIR /app
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN gradle bootJar --no-daemon --max-workers=2 -Dorg.gradle.jvmargs=-Xmx768m

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Xmx512m"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

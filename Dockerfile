FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/p2p-selector-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-Doracle.jdbc.timezoneAsRegion=false", "-jar", "app.jar"]
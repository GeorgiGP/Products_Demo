# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/product-catalog-*.jar app-1.0.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app-1.0.jar"]

# First stage: Build the JAR
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Second stage: Run the JAR
FROM eclipse-temurin:17-jdk
WORKDIR /
COPY --from=build /app/target/*.jar BooksApiTesting.jar
# Ensure environment variables are available
ENV DATABASE_URL=${DATABASE_URL}
ENV DATABASE_USERNAME=${DATABASE_USERNAME}
ENV DATABASE_PASSWORD=${DATABASE_PASSWORD}
ENV JWT_KEY=${JWT_KEY}
ENTRYPOINT ["java", "-Xmx2048M", "-jar", "/BooksApiTesting.jar"]


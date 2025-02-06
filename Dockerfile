# First stage: Build the JAR
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Second stage: Run the JAR
FROM openjdk:17-jdk
WORKDIR /
COPY --from=build /app/target/*.jar BooksApiTesting.jar
ENTRYPOINT ["java", "-Xmx2048M", "-jar", "/BooksApiTesting.jar"]


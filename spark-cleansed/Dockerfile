# Use an official Scala-SBT image as the base image
FROM hseeberger/scala-sbt:8u312_1.6.2_2.12.15 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the build.sbt file and project directory (if any)
COPY build.sbt ./
COPY . ./project/

# Download dependencies (this will cache them in the Docker layer)
RUN sbt update

# Copy the rest of the application source code
COPY src/ ./src/

# Build the application
RUN sbt assembly

# Use a smaller base image for the final stage
FROM openjdk:8-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/scala-2.12/MyScalaApp-assembly-1.0.jar ./app.jar

# Expose any necessary ports (e.g., for Kafka or Spark UI)
EXPOSE 4040 9092

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
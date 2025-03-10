# First stage
FROM hseeberger/scala-sbt:11.0.6_1.3.9_2.13.1 AS build

# Copy the entire project
COPY ./ ./

# Compile and package the application
# RUN sbt clean compile package
RUN sbt compile

# Second stage
# FROM openjdk:8-jre-alpine3.9 

# Copy the application JAR
# COPY --from=build /root/target/scala-2.12/*.jar /scala-spark-structured-streaming-app.jar

# Copy the Scala library JAR
# COPY --from=build /root/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.12.2.jar /scala-library-2.12.2.jar

# Copy all dependency JARs from the Ivy cache
# COPY --from=build /root/.ivy2/cache/*/jars/*.jar /libs/

# Expose any necessary ports (e.g., for Kafka or Spark UI)
EXPOSE 4040 9092

# Set the entry point for the application
# ENTRYPOINT ["java", "-cp", "/scala-spark-structured-streaming-app.jar:/scala-library-2.12.2.jar:/libs/*", "SparkStructuredStreaming"]
# ENTRYPOINT ["java", "-cp", "/scala-spark-structured-streaming-app.jar:/root/target/scala-2.12/*.jar:/root/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.12.2.jar:/root/.ivy2/cache/*/jars/*.jar", "SparkStructuredStreaming"]
ENTRYPOINT ["sbt", "run"]
# CMD []
// build.sbt
scalaVersion := "2.12.2"
version := "1.0"

name := "scala_app"
organization := "com.scala_app"

// Add Spark, Kafka, and Deequ dependencies
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.0",
  "org.apache.spark" %% "spark-sql" % "3.3.0",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.3.0", // Required for Kafka integration
  "org.apache.kafka" %% "kafka" % "2.8.1", // Kafka client library
  "com.amazon.deequ" % "deequ" % "2.0.8-spark-3.3" // Deequ for Spark 3.3
)

resolvers ++= Seq(
  "Maven Central Server"          at "https://repo1.maven.org/maven2"
)


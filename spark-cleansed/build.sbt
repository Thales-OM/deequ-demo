// build.sbt
name := "MyScalaApp"

version := "1.0"

scalaVersion := "2.12.15" // Use Scala 2.12.15

// Add Spark, Kafka, and Deequ dependencies
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.0",
  "org.apache.spark" %% "spark-sql" % "3.3.0",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.3.0", // Required for Kafka integration
  "org.apache.kafka" %% "kafka" % "2.8.1", // Kafka client library
  "com.amazon.deequ" % "deequ" % "2.0.8-spark-3.3" // Deequ for Spark 3.3
)

resolvers ++= Seq(
  "Sbt plugins"                   at "https://dl.bintray.com/sbt/sbt-plugin-releases",
  "Maven Central Server"          at "https://repo1.maven.org/maven2",
  "TypeSafe Repository Releases"  at "https://repo.typesafe.com/typesafe/releases/",
  "TypeSafe Repository Snapshots" at "https://repo.typesafe.com/typesafe/snapshots/"
)

lazy val app = (project in file("app"))
  .settings(
    assembly / mainClass := Some("com.spark.streaming.structured.SparkStructuredStreaming")
  )

lazy val utils = (project in file("utils"))
  .settings(
    assembly / assemblyJarName := "utils.jar"
  )
// build.sbt
name := "MyScalaApp"

version := "1.0"

scalaVersion := "2.12.15"

// Add Spark, Kafka, and Deequ dependencies
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.0",
  "org.apache.spark" %% "spark-sql" % "3.3.0",
  "org.apache.kafka" %% "kafka" % "3.2.0",
  "com.amazon.deequ" % "deequ" % "2.0.0-spark-3.3"
)

// Set the source directory to `src/` instead of `src/main/scala/`
Compile / scalaSource := baseDirectory.value / "src"
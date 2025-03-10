package com.spark.streaming.structured

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import com.amazon.deequ.{VerificationSuite, VerificationResult}
import com.amazon.deequ.checks.{Check, CheckLevel, CheckStatus}
import com.amazon.deequ.constraints.ConstraintStatus
import org.apache.spark.sql.DataFrame

object SparkStructuredStreaming {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("Spark Structured Streaming")
      .master("local[4]") // Adjust for cluster setup
      .getOrCreate()

    import spark.implicits._

    // Define the schema for the incoming data
    val schema = StructType(Array(
      StructField("id", IntegerType, true),
      StructField("timestamp", TimestampType, true),
      StructField("value", DoubleType, true),
      StructField("type", StringType, true)
    ))

    val kafka_bootstrap_servers = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
    val kafka_topic_name = sys.env.getOrElse("KAFKA_TOPIC_NAME", "data-stream")

    // Read stream from Kafka
    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafka_bootstrap_servers)
      .option("subscribe", kafka_topic_name)
      .load()

    // Extract the value as a string
    val values = df.selectExpr("CAST(value AS STRING) AS json_value")

    // Parse the JSON string into a DataFrame with inferred schema
    val parsedValues = values.select(from_json($"json_value", schema).as("data"))
      .select("data.*") // Flatten the DataFrame to get individual columns

    // Fetch output and checkpoint paths from environment variables
    val outputPath = sys.env.getOrElse("SPARK_OUTPUT_PATH", "/app/output") // Default path if not set
    val checkpointPath = sys.env.getOrElse("SPARK_CHECKPOINTS_PATH", "/app/checkpoints") // Default path if not set

    // Determine target table name for writing
    val target_table = sys.env.getOrElse("SPARK_TARGET_TABLE", "target_table")

    // Define a function to perform Deequ data quality checks
    def performDeequChecks(dataFrame: DataFrame): Unit = {
      val verificationResult = VerificationSuite()
        .onData(dataFrame)
        .addCheck(
          Check(CheckLevel.Error, "Data Quality Checks")
            .isComplete("id") // Ensure "id" column has no nulls
            .isComplete("timestamp") // Ensure "timestamp" column has no nulls
            .isNonNegative("value") // Ensure "value" column is non-negative
            .isContainedIn("type", Array("A", "B", "C")) // Ensure "type" column contains only allowed values
        )
        .run()

      // Print the verification result
      if (verificationResult.status == CheckStatus.Success) {
        println("Data quality checks passed!")
      } else {
        println("Data quality checks failed!")
      }
    }

    // Write to a Spark table (TARGET_TABLE)
    val query = parsedValues.writeStream
      .outputMode("append")
      .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
        // Perform Deequ data quality checks on each batch
        performDeequChecks(batchDF)

        // Write the batch DataFrame to the target table
        batchDF.write
          .format("parquet") // You can change this to "delta" or another format if needed
          .mode("append")
          .option("path", outputPath)
          .saveAsTable(target_table)
      }
      .option("checkpointLocation", checkpointPath) // Specify checkpoint location from environment variable
      .trigger(Trigger.ProcessingTime("1 second")) // Adjust the trigger interval for faster processing
      .start()

    query.awaitTermination()
  }
}
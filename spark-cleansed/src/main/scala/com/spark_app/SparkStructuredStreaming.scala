package com.spark_app

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{IntegerType, StringType, TimestampType}
import com.amazon.deequ.VerificationSuite
import com.amazon.deequ.VerificationResult
import com.amazon.deequ.checks.Check
import com.amazon.deequ.checks.CheckResult
import com.amazon.deequ.checks.CheckStatus
import com.amazon.deequ.checks.CheckLevel
import com.amazon.deequ.constraints.ConstraintStatus
import com.amazon.deequ.schema.{RowLevelSchema,RowLevelSchemaValidator}
import org.slf4j.LoggerFactory

object SparkStructuredStreaming {

  def main(args: Array[String]): Unit = {
    private val logger = LoggerFactory.getLogger(getClass)

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
    def performDeequChecks(dataFrame: DataFrame, batchId: Long): Unit = {
      val startTime = System.currentTimeMillis()
      val schema = RowLevelSchema()
        .withIntColumn("id", isNullable = false)
        .withStringColumn("type", isNullable = false, maxLength = Some(20))
        .withTimestampColumn("timestamp", mask = "yyyy-MM-dd HH:mm:ss", isNullable = false)

      val result = RowLevelSchemaValidator.validate(data, schema)

      val durationInSeconds = (System.currentTimeMillis() - startTime) / 1000.0 
      totalDeequChecksTime += durationInSeconds
      logger.info(s"[Batch #$batchId] Deequ validation completed in $durationInSeconds sec. (Total Deequ time = $totalDeequChecksTime sec)")
    }

    def process_batch(batchDF: DataFrame, batchId: Long): Unit = {
      performDeequChecks(batchDF, batchId)
      
      // Write the batch DataFrame to the target table
      batchDF.write
        .format("parquet") // You can change this to "delta" or another format if needed
        .mode("append")
        .option("path", outputPath)
        .saveAsTable(target_table)
      
      val endTime = System.currentTimeMillis()
      val durationInSeconds = (endTime - lastTotalCheckpointTime) / 1000.0 
      val totaldurationInSeconds = (endTime - totalStartTime) / 1000.0 
      lastTotalCheckpointTime = endTime
      logger.info(s"[Batch #$batchId] Processed and written in $durationInSeconds sec. (Total work time = $totaldurationInSeconds sec)")
    }

    val totalStartTime = System.currentTimeMillis()
    val lastTotalCheckpointTime = totalStartTime
    val totalDeequChecksTime: BigDecimal = BigDecimal(0.0)
    // Write to a Spark table (TARGET_TABLE)
    val query = parsedValues.writeStream
      .outputMode("append")
      .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
        // Perform Deequ data quality checks on each batch an write data to table
        process_batch(batchDF, batchId)
      }
      .option("checkpointLocation", checkpointPath) // Specify checkpoint location from environment variable
      .trigger(Trigger.ProcessingTime("1 second")) // Adjust the trigger interval for faster processing
      .start()

    query.awaitTermination()
  }
}
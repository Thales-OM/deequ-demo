# Kafka Data Streaming Project with Deequ

This project demonstrates the capabilities of the Deequ data quality library in Scala within a data streaming pipeline using Apache Kafka and Apache Spark. The architecture consists of multiple services orchestrated using Docker Compose, allowing for easy setup and execution of the entire pipeline.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Usage](#usage)
- [Environment Variables](#environment-variables)
- [License](#license)

## Overview

The primary goal of this project is to showcase how to implement data quality checks using the Deequ library in a real-time data processing scenario. The project includes the following components:

- **Kafka**: A distributed streaming platform that serves as the backbone for data transmission.
- **Data Generator**: A Python application that generates random data and sends it to a Kafka topic.
- **Kafka UI**: A web interface for monitoring Kafka topics and messages.
- **Spark Cleansed**: A Scala application that reads data from Kafka, performs data quality checks using Deequ, and writes the cleansed data to a specified output.

## Architecture

The architecture is defined in the `docker-compose.yml` file, which sets up the following services:

- **Kafka**: The main message broker.
- **Data Generator**: Generates and sends data to Kafka.
- **Kafka UI**: Provides a user interface to interact with Kafka.
- **Spark Cleansed**: Processes the data from Kafka, performs data quality checks using Deequ, and writes the cleansed data to a specified output.

## Prerequisites

- Docker and Docker Compose installed on your machine.
- Basic knowledge of Docker, Kafka, Spark, and data quality concepts.

## Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/Thales-OM/deequ-demo.git
   cd deequ-demo
   ``` 
2. Build and start the services using Docker Compose:

    ```bash
    docker-compose up --build
    ```
    This command will build the necessary Docker images and start all the services defined in docker-compose.yml.

## Usage

- Data Generation: The data generator will start automatically and begin sending data to the Kafka topic defined in the environment variables.
- Kafka UI: Access the Kafka UI at http://localhost:8082 to monitor the messages being sent to Kafka.
- Spark Processing: The Spark application will read from the Kafka topic, perform data quality checks, and write the cleansed data to the specified output location.

## Environment Variables

The following environment variables can be configured in the docker-compose.yml file, set in .env file or passed at runtime:

- `KAFKA_BROKER`: The address of the Kafka broker (default: kafka:9093).
- `KAFKA_TOPIC_NAME`: The name of the Kafka topic to which data will be sent (default: data-stream).
- `SPARK_OUTPUT_PATH`: The output path for the cleansed data (default: /app/output).
- `SPARK_CHECKPOINTS_PATH`: The checkpoint path for Spark streaming (default: /app/checkpoints).
- `SPARK_TARGET_TABLE`: The target table name for writing data (default: target_table).

## License

This project is licensed under the MIT License.
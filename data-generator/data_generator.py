from kafka import KafkaProducer
import pandas as pd
import numpy as np
import time
import os
import json
import argparse
import signal
import logging
from logger_setup import setup_logging

# Configuration
KAFKA_BROKERS = os.getenv('KAFKA_BROKERS', 'kafka:9093')
KAFKA_TOPIC_NAME = os.getenv('KAFKA_TOPIC_NAME', 'data-stream')
BATCH_SIZE = 50000
DEFAULT_SLEEP_TIME = 1.0
SEED = 42

# Set up logging
setup_logging()
logger = logging.getLogger(__name__)

# Signal handler for graceful shutdown
shutdown_flag = False
def signal_handler(sig, frame):
    global shutdown_flag
    logger.info("\nReceived termination signal, shutting down...")
    shutdown_flag = True

signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)


def create_producer():
    return KafkaProducer(
        bootstrap_servers=KAFKA_BROKERS,
        value_serializer=lambda v: json.dumps(v).encode('utf-8'),
        # batch_size=16384,
        linger_ms=100
    )

def main():
    parser = argparse.ArgumentParser(description='Data Generator')
    parser.add_argument('--batches', type=int, help='Number of batches to generate')
    parser.add_argument('--time', type=int, help='Run duration in seconds')
    parser.add_argument('--sleep-time', type=float, help='Time to sleep between sending batches, in seconds', default=DEFAULT_SLEEP_TIME)
    args = parser.parse_args()

    output_dir = "/app/data"
    batch_size = BATCH_SIZE
    batch_count = 0
    start_time = time.time()

    # Create a random number generator with a seed
    rng = np.random.default_rng(seed=SEED)

    # Start Kafka producer
    producer = create_producer()
    logger.info(f"Started Kafka producer for topic: {KAFKA_TOPIC_NAME}")

    while not shutdown_flag:
        # Check termination conditions
        if args.batches and batch_count >= args.batches:
            logger.info(f"Generated {args.batches} batches. Exiting.")
            break
            
        if args.time and (time.time() - start_time) >= args.time:
            logger.info(f"Reached {args.time} second runtime. Exiting.")
            break

        # Generate data
        batch = {
            'id': batch_count * batch_size + np.arange(batch_size),
            'timestamp': [time.time()] * batch_size,
            'value': rng.random(batch_size), # Random floats in [0.0, 1.0)
            'type': rng.choice(('car', 'bike', 'plane', 'motorcycle'), size=batch_size)
        }
        batch_count += 1
        logger.debug(f"Generated batch #{batch_count}.")

        producer.send(KAFKA_TOPIC_NAME, value=batch)
        logger.debug(f"Batch #{batch_count} sent to Kafka.")
    
        logger.debug(f"Sleeping for {args.sleep_time} seconds...")
        time.sleep(args.sleep_time)

if __name__ == "__main__":
    main()
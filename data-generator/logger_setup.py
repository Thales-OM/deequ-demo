import logging
import logging.config
import os

def setup_logging(default_path='logging.ini', default_level=logging.INFO):
    """Setup logging configuration."""
    if os.path.exists(default_path):
        logging.config.fileConfig(default_path)
    else:
        logging.basicConfig(level=default_level)
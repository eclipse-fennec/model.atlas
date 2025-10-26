"""Logging configuration for the dummy data generator."""

import sys
from loguru import logger
from src.config.settings import settings


def setup_logging():
    """Configure logging with loguru."""
    # Remove default handler
    logger.remove()
    
    # Add custom handler with configuration from settings
    logger.add(
        sys.stdout,
        format=settings.log_format,
        level=settings.log_level,
        colorize=True,
        backtrace=True,
        diagnose=True
    )
    
    # Add file handler for persistent logging
    logger.add(
        "logs/dummy_data_generator.log",
        format=settings.log_format,
        level=settings.log_level,
        rotation="1 day",
        retention="7 days",
        compression="zip"
    )
    
    return logger


def get_logger(name: str):
    """Get a logger instance with the given name."""
    return logger.bind(name=name)

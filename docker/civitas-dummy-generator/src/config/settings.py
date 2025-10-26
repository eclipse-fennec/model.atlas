"""Configuration settings for the dummy data generator."""

from pydantic import Field
from pydantic_settings import BaseSettings
from typing import Optional
import os


class Settings(BaseSettings):
    """Main application settings."""
    
    # Database settings
    db_host: str = Field(default="localhost", env="DB_HOST")
    db_port: int = Field(default=5432, env="DB_PORT")
    db_user: str = Field(default="postgres", env="DB_USER")
    db_password: str = Field(default="postgres", env="DB_PASSWORD")
    
    # Database names for different use cases
    remote_meters_db: str = Field(default="usecase206_remote_meters", env="REMOTE_METERS_DB")
    manual_meters_db: str = Field(default="usecase206_manual_meters", env="MANUAL_METERS_DB")
    manual_readings_db: str = Field(default="usecase206_manual_readings", env="MANUAL_READINGS_DB")
    assets_db: str = Field(default="usecase212_assets", env="ASSETS_DB")
    
    # MinIO settings
    minio_endpoint: str = Field(default="localhost:9000", env="MINIO_ENDPOINT")
    minio_access_key: str = Field(default="minio", env="MINIO_ACCESS_KEY")
    minio_secret_key: str = Field(default="minio123", env="MINIO_SECRET_KEY")
    minio_bucket: str = Field(default="dummy-data", env="MINIO_BUCKET")
    minio_secure: bool = Field(default=False, env="MINIO_SECURE")
    
    # MQTT settings
    mqtt_host: str = Field(default="localhost", env="MQTT_HOST")
    mqtt_port: int = Field(default=1883, env="MQTT_PORT")
    mqtt_username: Optional[str] = Field(default=None, env="MQTT_USERNAME")
    mqtt_password: Optional[str] = Field(default=None, env="MQTT_PASSWORD")
    mqtt_topic_prefix: str = Field(default="buildings", env="MQTT_TOPIC_PREFIX")
    
    # Job scheduling settings
    usecase206_interval: int = Field(default=60, env="USECASE206_INTERVAL")
    usecase212_interval: int = Field(default=5, env="USECASE212_INTERVAL")
    
    # Data generation settings
    num_plants: int = Field(default=10, env="NUM_PLANTS")
    num_parts: int = Field(default=10, env="NUM_PARTS")
    num_buildings: int = Field(default=5, env="NUM_BUILDINGS")
    sine_period: int = Field(default=60, env="SINE_PERIOD")
    
    # Logging settings
    log_level: str = Field(default="INFO", env="LOG_LEVEL")
    log_format: str = Field(
        default="{time:YYYY-MM-DD HH:mm:ss} | {level} | {name}:{function}:{line} | {message}",
        env="LOG_FORMAT"
    )
    
    model_config = {
        "env_file": ".env",
        "env_file_encoding": "utf-8"
    }


# Global settings instance
settings = Settings()

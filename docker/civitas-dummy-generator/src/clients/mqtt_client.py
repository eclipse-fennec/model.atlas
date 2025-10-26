"""MQTT client for publishing sensor data."""

import json
from typing import Dict, Any, Optional
import paho.mqtt.client as mqtt
from src.config.settings import settings
from src.utils.logging import get_logger

logger = get_logger(__name__)


class MQTTClient:
    """MQTT client for publishing data streams."""
    
    def __init__(self):
        self.host = settings.mqtt_host
        self.port = settings.mqtt_port
        self.username = settings.mqtt_username
        self.password = settings.mqtt_password
        self.topic_prefix = settings.mqtt_topic_prefix
        
        # Initialize MQTT client
        self.client = mqtt.Client()
        
        # Set up authentication if provided
        if self.username and self.password:
            self.client.username_pw_set(self.username, self.password)
        
        # Set up callbacks
        self.client.on_connect = self._on_connect
        self.client.on_disconnect = self._on_disconnect
        self.client.on_publish = self._on_publish
        
        self.connected = False
    
    def _on_connect(self, client, userdata, flags, rc):
        """Callback for when the client receives a CONNACK response from the server."""
        if rc == 0:
            self.connected = True
            logger.info(f"Connected to MQTT broker at {self.host}:{self.port}")
        else:
            self.connected = False
            logger.error(f"Failed to connect to MQTT broker, return code {rc}")
    
    def _on_disconnect(self, client, userdata, rc):
        """Callback for when the client disconnects from the server."""
        self.connected = False
        if rc != 0:
            logger.warning("Unexpected disconnection from MQTT broker")
        else:
            logger.info("Disconnected from MQTT broker")
    
    def _on_publish(self, client, userdata, mid):
        """Callback for when a message is published."""
        logger.debug(f"Message {mid} published successfully")
    
    def connect(self) -> bool:
        """
        Connect to the MQTT broker.
        
        Returns:
            True if connection successful, False otherwise
        """
        try:
            self.client.connect(self.host, self.port, 60)
            self.client.loop_start()  # Start the network loop in a separate thread
            
            # Wait a moment for connection to establish
            import time
            time.sleep(1)
            
            return self.connected
        except Exception as e:
            logger.error(f"Failed to connect to MQTT broker: {e}")
            return False
    
    def disconnect(self):
        """Disconnect from the MQTT broker."""
        self.client.loop_stop()
        self.client.disconnect()
        self.connected = False
        logger.info("Disconnected from MQTT broker")
    
    def publish_building_data(self, building_id: int, value: float, timestamp: str) -> bool:
        """
        Publish building sensor data to MQTT.
        
        Args:
            building_id: ID of the building
            value: Sensor reading value
            timestamp: Timestamp of the reading
        
        Returns:
            True if published successfully, False otherwise
        """
        if not self.connected:
            logger.warning("Not connected to MQTT broker, attempting to reconnect...")
            if not self.connect():
                return False
        
        topic = f"{self.topic_prefix}/{building_id}"
        
        payload = {
            "building_id": str(building_id),
            "value": value,
            "timestamp": timestamp
        }
        
        try:
            result = self.client.publish(topic, json.dumps(payload), qos=1)
            
            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                logger.debug(f"Published data for building {building_id}: {payload}")
                return True
            else:
                logger.error(f"Failed to publish data for building {building_id}, error code: {result.rc}")
                return False
                
        except Exception as e:
            logger.error(f"Exception while publishing data for building {building_id}: {e}")
            return False
    
    def publish_json(self, topic: str, data: Dict[str, Any], qos: int = 1) -> bool:
        """
        Publish JSON data to a specific topic.
        
        Args:
            topic: MQTT topic to publish to
            data: Data to publish as JSON
            qos: Quality of Service level (0, 1, or 2)
        
        Returns:
            True if published successfully, False otherwise
        """
        if not self.connected:
            logger.warning("Not connected to MQTT broker, attempting to reconnect...")
            if not self.connect():
                return False
        
        try:
            result = self.client.publish(topic, json.dumps(data), qos=qos)
            
            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                logger.debug(f"Published to topic {topic}: {data}")
                return True
            else:
                logger.error(f"Failed to publish to topic {topic}, error code: {result.rc}")
                return False
                
        except Exception as e:
            logger.error(f"Exception while publishing to topic {topic}: {e}")
            return False
    
    def publish_raw(self, topic: str, payload: str, qos: int = 1) -> bool:
        """
        Publish raw string payload to a topic.
        
        Args:
            topic: MQTT topic to publish to
            payload: Raw string payload
            qos: Quality of Service level (0, 1, or 2)
        
        Returns:
            True if published successfully, False otherwise
        """
        if not self.connected:
            logger.warning("Not connected to MQTT broker, attempting to reconnect...")
            if not self.connect():
                return False
        
        try:
            result = self.client.publish(topic, payload, qos=qos)
            
            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                logger.debug(f"Published raw payload to topic {topic}")
                return True
            else:
                logger.error(f"Failed to publish to topic {topic}, error code: {result.rc}")
                return False
                
        except Exception as e:
            logger.error(f"Exception while publishing to topic {topic}: {e}")
            return False
    
    def is_connected(self) -> bool:
        """Check if client is connected to broker."""
        return self.connected


# Global client instance
mqtt_client = MQTTClient()

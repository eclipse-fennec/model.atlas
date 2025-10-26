"""Use Case #212: MQTT Data Stream job."""

from src.jobs.base import MQTTJob
from src.data_generators.building_data import building_data_generator
from src.clients.mqtt_client import mqtt_client


class MQTTStreamJob(MQTTJob):
    """Job for streaming building sensor data to MQTT broker."""
    
    def __init__(self):
        super().__init__(
            name="mqtt_stream",
            description="Stream building sensor data to MQTT broker every 5 seconds"
        )
    
    def execute(self) -> bool:
        """Execute the MQTT Stream job."""
        try:
            # Ensure MQTT connection
            if not self.ensure_mqtt_connection(mqtt_client):
                self.logger.error("Failed to connect to MQTT broker")
                return False
            
            # Get all building IDs
            building_ids = building_data_generator.get_all_building_ids()
            
            if not building_ids:
                self.logger.error("No building IDs found")
                return False
            
            # Generate and publish sensor readings for all buildings
            successful_publishes = 0
            for building_id in building_ids:
                try:
                    # Generate sensor reading for this building
                    reading = building_data_generator.generate_sensor_reading(building_id)
                    
                    # Publish to MQTT
                    success = mqtt_client.publish_building_data(
                        building_id,
                        reading['value'],
                        reading['timestamp']
                    )
                    
                    if success:
                        successful_publishes += 1
                        self.logger.debug(f"Published reading for building {building_id}: {reading['value']}")
                    else:
                        self.logger.warning(f"Failed to publish reading for building {building_id}")
                    
                except Exception as e:
                    self.logger.error(f"Failed to process building {building_id}: {e}")
                    continue
            
            success_rate = successful_publishes / len(building_ids) if building_ids else 0
            self.logger.info(f"MQTT stream job completed: {successful_publishes}/{len(building_ids)} readings published ({success_rate:.1%})")
            
            return success_rate > 0.8  # Consider successful if >80% of readings published
            
        except Exception as e:
            self.logger.error(f"MQTT Stream job failed: {e}")
            return False


# Global job instance
mqtt_stream_job = MQTTStreamJob()

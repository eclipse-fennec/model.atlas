"""Source System #4: Manual Meters Database job for Use Case #206."""

from src.jobs.base import DatabaseJob
from src.data_generators.meter_data import meter_data_generator
from src.clients.postgres_client import postgres_client
from src.config.settings import settings


class ManualMetersJob(DatabaseJob):
    """Job for generating and inserting manual meter readings to PostgreSQL."""
    
    def __init__(self):
        super().__init__(
            name="manual_meters",
            description="Generate manual meter readings and insert to PostgreSQL"
        )
        self.meters_database = settings.manual_meters_db
        self.readings_database = settings.manual_readings_db
    
    def execute(self) -> bool:
        """Execute the Manual Meters job."""
        try:
            # Get all plant IDs for meters
            plant_ids = meter_data_generator.get_all_plant_ids_for_meters()
            
            if not plant_ids:
                self.logger.error("No plant IDs found for meters")
                return False
            
            # Ensure meters exist for all plant IDs in the meters database
            meter_ids = []
            for plant_id in plant_ids:
                try:
                    meter_id = postgres_client.create_meter_if_not_exists(self.meters_database, plant_id)
                    meter_ids.append((meter_id, plant_id))
                except Exception as e:
                    self.logger.error(f"Failed to create/get meter for plant {plant_id}: {e}")
                    continue
            
            if not meter_ids:
                self.logger.error("No meters available for readings")
                return False
            
            # Generate and insert meter readings to the readings database
            successful_inserts = 0
            for meter_id, plant_id in meter_ids:
                try:
                    # Generate reading for this plant
                    reading = meter_data_generator.generate_single_reading(plant_id)
                    
                    # Insert reading to the manual readings database
                    result = postgres_client.insert_manual_reading(
                        self.readings_database,
                        meter_id,
                        reading['timestamp'],
                        reading['value']
                    )
                    
                    if result:
                        successful_inserts += 1
                        self.logger.debug(f"Inserted manual reading for meter {meter_id}: {reading['value']}")
                    
                except Exception as e:
                    self.logger.error(f"Failed to insert manual reading for meter {meter_id}: {e}")
                    continue
            
            success_rate = successful_inserts / len(meter_ids) if meter_ids else 0
            self.logger.info(f"Manual meters job completed: {successful_inserts}/{len(meter_ids)} readings inserted ({success_rate:.1%})")
            
            return success_rate > 0.8  # Consider successful if >80% of readings inserted
            
        except Exception as e:
            self.logger.error(f"Manual Meters job failed: {e}")
            return False


# Global job instance
manual_meters_job = ManualMetersJob()

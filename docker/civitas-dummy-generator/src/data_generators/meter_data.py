"""Meter data generators for Use Case #206."""

from typing import List, Dict, Any
from src.config.settings import settings
from src.utils.helpers import generate_sinusoidal_value, get_current_timestamp
from src.utils.logging import get_logger
from src.data_generators.plant_data import plant_data_generator

logger = get_logger(__name__)


class MeterDataGenerator:
    """Generator for meter reading data."""
    
    def __init__(self):
        self.sine_period = settings.sine_period
    
    def generate_meter_readings(self, plant_ids: List[str]) -> List[Dict[str, Any]]:
        """
        Generate sinusoidal meter readings for all plant IDs.
        
        Args:
            plant_ids: List of plant IDs (without 'p' prefix)
        
        Returns:
            List of meter reading records
        """
        readings = []
        current_time = get_current_timestamp()
        
        for plant_id in plant_ids:
            # Generate sinusoidal value with unique phase offset for each plant
            phase_offset = hash(plant_id) % 100 / 100.0 * 6.28  # Convert to radians
            
            value = generate_sinusoidal_value(
                base_value=0.0,
                amplitude=100.0,
                period_seconds=self.sine_period,
                phase_offset=phase_offset
            )
            
            reading = {
                'plant_id': plant_id,
                'timestamp': current_time.strftime('%Y-%m-%d %H:%M:%S'),
                'value': round(value, 2)
            }
            readings.append(reading)
        
        logger.info(f"Generated {len(readings)} meter readings")
        return readings
    
    def generate_single_reading(self, plant_id: str) -> Dict[str, Any]:
        """
        Generate a single meter reading for a specific plant.
        
        Args:
            plant_id: Plant ID (without 'p' prefix)
        
        Returns:
            Single meter reading record
        """
        current_time = get_current_timestamp()
        
        # Generate sinusoidal value with unique phase offset for this plant
        phase_offset = hash(plant_id) % 100 / 100.0 * 6.28  # Convert to radians
        
        value = generate_sinusoidal_value(
            base_value=0.0,
            amplitude=100.0,
            period_seconds=self.sine_period,
            phase_offset=phase_offset
        )
        
        reading = {
            'plant_id': plant_id,
            'timestamp': current_time.strftime('%Y-%m-%d %H:%M:%S'),
            'value': round(value, 2)
        }
        
        logger.debug(f"Generated reading for plant {plant_id}: {value:.2f}")
        return reading
    
    def get_all_plant_ids_for_meters(self) -> List[str]:
        """Get all plant IDs that should have meters (without 'p' prefix)."""
        return plant_data_generator.get_all_plant_ids()


# Global generator instance
meter_data_generator = MeterDataGenerator()

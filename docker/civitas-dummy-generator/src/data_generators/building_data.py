"""Building data generators for Use Case #212."""

from typing import List, Dict, Any
from faker import Faker
from src.config.settings import settings
from src.utils.helpers import generate_sinusoidal_value, get_current_timestamp
from src.utils.logging import get_logger

logger = get_logger(__name__)
fake = Faker()


class BuildingDataGenerator:
    """Generator for building and sensor data."""
    
    def __init__(self):
        self.num_buildings = settings.num_buildings
        self.sine_period = settings.sine_period
    
    def generate_buildings(self) -> List[Dict[str, Any]]:
        """
        Generate building master data.
        Always generates exactly 5 buildings.
        
        Returns:
            List of building records
        """
        buildings = []
        
        # Always generate exactly 5 buildings
        for i in range(1, 6):
            building = {
                'id': i,
                'city': fake.city(),
                'zip': fake.zipcode_in_state(),
                'street': f"{fake.street_name()} {fake.building_number()}"
            }
            buildings.append(building)
        
        logger.info(f"Generated {len(buildings)} building records")
        return buildings
    
    def generate_contacts(self, building_ids: List[int]) -> List[Dict[str, Any]]:
        """
        Generate contact data for buildings.
        Always generates exactly 2 contacts per building (10 total for 5 buildings).
        Each building gets exactly one "Hausmeister" and one other role.
        
        Args:
            building_ids: List of building IDs
        
        Returns:
            List of contact records
        """
        contacts = []
        other_roles = ['Manager', 'Technician', 'Administrator']
        
        for building_id in building_ids:
            # First contact: Always a Hausmeister
            hausmeister_contact = {
                'building_id': building_id,
                'role': 'Hausmeister',
                'email': fake.email(),
                'phonenumber': fake.phone_number(),
                'first_name': fake.first_name(),
                'last_name': fake.last_name()
            }
            contacts.append(hausmeister_contact)
            
            # Second contact: One other role
            other_contact = {
                'building_id': building_id,
                'role': fake.random_element(other_roles),
                'email': fake.email(),
                'phonenumber': fake.phone_number(),
                'first_name': fake.first_name(),
                'last_name': fake.last_name()
            }
            contacts.append(other_contact)
        
        logger.info(f"Generated {len(contacts)} contact records for {len(building_ids)} buildings")
        return contacts
    
    def generate_sensor_reading(self, building_id: int) -> Dict[str, Any]:
        """
        Generate a single sensor reading for a building.
        
        Args:
            building_id: ID of the building
        
        Returns:
            Sensor reading record
        """
        current_time = get_current_timestamp()
        
        # Generate sinusoidal value with unique phase offset for each building
        phase_offset = hash(str(building_id)) % 100 / 100.0 * 6.28  # Convert to radians
        
        value = generate_sinusoidal_value(
            base_value=20.0,
            amplitude=20.0,
            period_seconds=self.sine_period,
            phase_offset=phase_offset
        )
        
        reading = {
            'building_id': str(building_id),
            'value': round(value, 2),
            'timestamp': current_time.isoformat()
        }
        
        logger.debug(f"Generated sensor reading for building {building_id}: {value:.2f}")
        return reading
    
    def generate_all_sensor_readings(self) -> List[Dict[str, Any]]:
        """
        Generate sensor readings for all buildings.
        
        Returns:
            List of sensor reading records
        """
        readings = []
        
        for building_id in range(1, self.num_buildings + 1):
            reading = self.generate_sensor_reading(building_id)
            readings.append(reading)
        
        logger.info(f"Generated {len(readings)} sensor readings")
        return readings
    
    def get_all_building_ids(self) -> List[int]:
        """Get list of all building IDs. Always returns exactly 5 building IDs (1-5)."""
        return list(range(1, 6))


# Global generator instance
building_data_generator = BuildingDataGenerator()

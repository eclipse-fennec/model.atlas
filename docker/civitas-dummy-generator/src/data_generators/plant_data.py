"""Plant and asset data generators for Use Case #206."""

import random
from typing import List, Dict, Any
from faker import Faker
from src.config.settings import settings
from src.utils.helpers import generate_plant_id, increment_counter
from src.utils.logging import get_logger

logger = get_logger(__name__)
fake = Faker()


class PlantDataGenerator:
    """Generator for plant and asset master data."""
    
    def __init__(self):
        self.num_plants = settings.num_plants
        self.num_parts = settings.num_parts
        self.counter_file = "data/counters/address_counter.txt"
    
    def generate_basic_data(self) -> List[Dict[str, Any]]:
        """
        Generate basic asset master data (plants + parts).
        
        Returns:
            List of plant/asset records
        """
        records = []
        
        # Generate plants (with type)
        for i in range(1, self.num_plants + 1):
            plant_id = generate_plant_id(i)
            
            # Get current address counter for this plant
            address_counter = self._get_address_counter(plant_id)
            
            record = {
                'plant_id': plant_id,
                'city': fake.city(),
                'zip': fake.zipcode_in_state(),
                'street': f"{fake.street_name()} {address_counter}",
                'type': random.choice(['Industrial', 'Commercial', 'Residential', 'Mixed'])
            }
            records.append(record)
        
        # Generate parts (without type)
        for i in range(self.num_plants + 1, self.num_plants + self.num_parts + 1):
            plant_id = generate_plant_id(i)
            
            # Get current address counter for this part
            address_counter = self._get_address_counter(plant_id)
            
            record = {
                'plant_id': plant_id,
                'city': fake.city(),
                'zip': fake.zipcode_in_state(),
                'street': f"{fake.street_name()} {address_counter}",
                'type': None  # Parts don't have type
            }
            records.append(record)
        
        logger.info(f"Generated {len(records)} basic data records ({self.num_plants} plants + {self.num_parts} parts)")
        return records
    
    def _get_address_counter(self, plant_id: str) -> int:
        """Get current address counter for a specific plant."""
        counter_file = f"data/counters/address_{plant_id}.txt"
        return increment_counter(counter_file, start_value=1)
    
    def update_address(self, plant_id: str) -> Dict[str, Any]:
        """
        Update address for a specific plant (increment house number).
        
        Args:
            plant_id: ID of the plant to update
        
        Returns:
            Updated plant record
        """
        # Increment address counter
        new_house_number = self._get_address_counter(plant_id)
        
        record = {
            'plant_id': plant_id,
            'city': fake.city(),  # Could keep same city, but generating new for variety
            'zip': fake.zipcode_in_state(),
            'street': f"{fake.street_name()} {new_house_number}",
            'type': random.choice(['Industrial', 'Commercial', 'Residential', 'Mixed'])
        }
        
        logger.info(f"Updated address for {plant_id}: {record['street']}")
        return record
    
    def generate_operating_data(self) -> List[Dict[str, Any]]:
        """
        Generate operating data for all plants and parts.
        
        Returns:
            List of operating data records
        """
        records = []
        
        # Generate operating data for plants
        for i in range(1, self.num_plants + 1):
            plant_id_with_prefix = generate_plant_id(i)
            plant_id_without_prefix = str(i).zfill(3)  # Remove 'p' prefix
            
            # Get current operating hours (incremental)
            operating_hours = self._get_operating_hours(plant_id_with_prefix)
            
            # Get next sequential ID for each record
            current_id = increment_counter("data/counters/operating_id.txt", start_value=1)
            
            record = {
                'id': current_id,
                'plant_id': plant_id_without_prefix,
                'operating_hours': operating_hours
            }
            records.append(record)
        
        # Generate operating data for parts
        for i in range(self.num_plants + 1, self.num_plants + self.num_parts + 1):
            plant_id_with_prefix = generate_plant_id(i)
            plant_id_without_prefix = str(i).zfill(3)  # Remove 'p' prefix
            
            # Get current operating hours (incremental)
            operating_hours = self._get_operating_hours(plant_id_with_prefix)
            
            # Get next sequential ID for each record
            current_id = increment_counter("data/counters/operating_id.txt", start_value=1)
            
            record = {
                'id': current_id,
                'plant_id': plant_id_without_prefix,
                'operating_hours': operating_hours
            }
            records.append(record)
        
        logger.info(f"Generated {len(records)} operating data records")
        return records
    
    def _get_operating_hours(self, plant_id: str) -> int:
        """Get incremented operating hours for a specific plant."""
        counter_file = f"data/counters/operating_hours_{plant_id}.txt"
        return increment_counter(counter_file, start_value=100)  # Start at 100 hours
    
    def get_all_plant_ids(self) -> List[str]:
        """Get list of all plant IDs (without 'p' prefix)."""
        plant_ids = []
        
        # Plants
        for i in range(1, self.num_plants + 1):
            plant_ids.append(str(i).zfill(3))
        
        # Parts
        for i in range(self.num_plants + 1, self.num_plants + self.num_parts + 1):
            plant_ids.append(str(i).zfill(3))
        
        return plant_ids
    
    def get_plant_ids_with_prefix(self) -> List[str]:
        """Get list of all plant IDs (with 'p' prefix)."""
        plant_ids = []
        
        # Plants
        for i in range(1, self.num_plants + 1):
            plant_ids.append(generate_plant_id(i))
        
        # Parts
        for i in range(self.num_plants + 1, self.num_plants + self.num_parts + 1):
            plant_ids.append(generate_plant_id(i))
        
        return plant_ids


# Global generator instance
plant_data_generator = PlantDataGenerator()

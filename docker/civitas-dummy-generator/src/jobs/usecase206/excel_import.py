"""Source System #1: Excel Import (CSV) job for Use Case #206."""

import random
from typing import List, Dict, Any
from src.jobs.base import CSVJob
from src.data_generators.plant_data import plant_data_generator
from src.clients.minio_client import minio_client
from src.utils.helpers import format_filename_timestamp


class ExcelImportJob(CSVJob):
    """Job for generating and uploading asset master data CSV files to MinIO."""
    
    def __init__(self):
        super().__init__(
            name="excel_import",
            description="Generate asset master data CSV and upload to MinIO"
        )
        # Persistent data storage in memory
        self._persistent_data: List[Dict[str, Any]] = []
        self._is_initialized = False
    
    def _initialize_data(self) -> None:
        """Initialize persistent data on first run."""
        if not self._is_initialized:
            self.logger.info("Initializing persistent data for excel_import job")
            self._persistent_data = plant_data_generator.generate_basic_data()
            self._is_initialized = True
            self.logger.info(f"Initialized {len(self._persistent_data)} persistent records")
    
    def _increment_random_house_number(self) -> None:
        """Increment house number of a randomly selected record."""
        if not self._persistent_data:
            return
        
        # Select a random record
        random_record = random.choice(self._persistent_data)
        
        # Extract current house number from street
        street_parts = random_record['street'].split()
        if street_parts:
            try:
                # Try to find and increment the house number (last part that's a number)
                for i in range(len(street_parts) - 1, -1, -1):
                    if street_parts[i].isdigit():
                        current_house_number = int(street_parts[i])
                        new_house_number = current_house_number + 1
                        street_parts[i] = str(new_house_number)
                        random_record['street'] = ' '.join(street_parts)
                        self.logger.info(f"Incremented house number for {random_record['plant_id']}: {random_record['street']}")
                        return
                
                # If no number found, append one
                random_record['street'] += " 1"
                self.logger.info(f"Added house number for {random_record['plant_id']}: {random_record['street']}")
                
            except (ValueError, IndexError):
                # If parsing fails, just append a number
                random_record['street'] += " 1"
                self.logger.info(f"Added house number for {random_record['plant_id']}: {random_record['street']}")
    
    def execute(self) -> bool:
        """Execute the Excel Import job."""
        try:
            # On subsequent runs, increment a random house number
            if self._is_initialized:
                self._increment_random_house_number()
            else: 
                # Initialize data on first run
                self._initialize_data()
            
            # Use persistent data
            data = self._persistent_data.copy()  # Create a copy to avoid modifying original
            
            if not data:
                self.logger.error("No data available")
                return False
            
            # Define CSV headers
            headers = ['plant_id', 'city', 'zip', 'street', 'type']
            
            # Generate CSV content
            csv_content = self.generate_csv_content(data, headers)
            
            # Generate filename with timestamp
            timestamp = format_filename_timestamp()
            filename = f"basic_data_{timestamp}.csv"
            
            # Upload to MinIO
            success = minio_client.upload_csv_content(csv_content, filename)
            
            if success:
                self.logger.info(f"Successfully uploaded {filename} with {len(data)} records")
                return True
            else:
                self.logger.error(f"Failed to upload {filename}")
                return False
                
        except Exception as e:
            self.logger.error(f"Excel Import job failed: {e}")
            return False


# Global job instance
excel_import_job = ExcelImportJob()

"""Source System #2: Operating Data CSV job for Use Case #206."""

from src.jobs.base import CSVJob
from src.data_generators.plant_data import plant_data_generator
from src.clients.minio_client import minio_client
from src.utils.helpers import format_filename_timestamp


class OperatingDataJob(CSVJob):
    """Job for generating and uploading operating data CSV files to MinIO."""
    
    def __init__(self):
        super().__init__(
            name="operating_data",
            description="Generate operating data CSV and upload to MinIO"
        )
    
    def execute(self) -> bool:
        """Execute the Operating Data job."""
        try:
            # Generate operating data
            data = plant_data_generator.generate_operating_data()
            
            if not data:
                self.logger.error("No operating data generated")
                return False
            
            # Define CSV headers
            headers = ['id', 'plant_id', 'operating_hours']
            
            # Generate CSV content
            csv_content = self.generate_csv_content(data, headers)
            
            # Generate filename with timestamp
            timestamp = format_filename_timestamp()
            filename = f"operating_data_{timestamp}.csv"
            
            # Upload to MinIO
            success = minio_client.upload_csv_content(csv_content, filename)
            
            if success:
                self.logger.info(f"Successfully uploaded {filename} with {len(data)} records")
                return True
            else:
                self.logger.error(f"Failed to upload {filename}")
                return False
                
        except Exception as e:
            self.logger.error(f"Operating Data job failed: {e}")
            return False


# Global job instance
operating_data_job = OperatingDataJob()

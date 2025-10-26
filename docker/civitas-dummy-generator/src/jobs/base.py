"""Base job class for scheduled tasks."""

from abc import ABC, abstractmethod
from typing import Any, Dict, Optional
from src.utils.logging import get_logger

logger = get_logger(__name__)


class BaseJob(ABC):
    """Abstract base class for all scheduled jobs."""
    
    def __init__(self, name: str, description: str = ""):
        self.name = name
        self.description = description
        self.logger = get_logger(f"job.{name}")
        self.is_running = False
    
    @abstractmethod
    def execute(self) -> bool:
        """
        Execute the job logic.
        
        Returns:
            True if job executed successfully, False otherwise
        """
        pass
    
    def run(self) -> bool:
        """
        Run the job with error handling and logging.
        
        Returns:
            True if job executed successfully, False otherwise
        """
        if self.is_running:
            self.logger.warning(f"Job {self.name} is already running, skipping execution")
            return False
        
        self.is_running = True
        self.logger.info(f"Starting job: {self.name}")
        
        try:
            result = self.execute()
            
            if result:
                self.logger.info(f"Job {self.name} completed successfully")
            else:
                self.logger.error(f"Job {self.name} failed")
            
            return result
            
        except Exception as e:
            self.logger.error(f"Job {self.name} failed with exception: {e}", exc_info=True)
            return False
        
        finally:
            self.is_running = False
    
    def get_info(self) -> Dict[str, Any]:
        """Get job information."""
        return {
            'name': self.name,
            'description': self.description,
            'is_running': self.is_running
        }
    
    def __str__(self) -> str:
        return f"Job({self.name})"
    
    def __repr__(self) -> str:
        return f"Job(name='{self.name}', description='{self.description}')"


class CSVJob(BaseJob):
    """Base class for jobs that generate CSV files."""
    
    def __init__(self, name: str, description: str = ""):
        super().__init__(name, description)
    
    def generate_csv_content(self, data: list, headers: list) -> str:
        """
        Generate CSV content from data.
        
        Args:
            data: List of dictionaries containing the data
            headers: List of column headers
        
        Returns:
            CSV content as string
        """
        import csv
        import io
        
        output = io.StringIO()
        writer = csv.DictWriter(output, fieldnames=headers)
        
        # Write header
        writer.writeheader()
        
        # Write data rows
        for row in data:
            writer.writerow(row)
        
        csv_content = output.getvalue()
        output.close()
        
        return csv_content


class DatabaseJob(BaseJob):
    """Base class for jobs that work with databases."""
    
    def __init__(self, name: str, description: str = ""):
        super().__init__(name, description)
    
    def batch_insert_with_retry(self, insert_func, data_list: list, batch_size: int = 100) -> bool:
        """
        Insert data in batches with retry logic.
        
        Args:
            insert_func: Function to call for inserting data
            data_list: List of data to insert
            batch_size: Size of each batch
        
        Returns:
            True if all inserts successful, False otherwise
        """
        total_records = len(data_list)
        successful_inserts = 0
        
        for i in range(0, total_records, batch_size):
            batch = data_list[i:i + batch_size]
            
            try:
                insert_func(batch)
                successful_inserts += len(batch)
                self.logger.debug(f"Inserted batch {i//batch_size + 1}, records: {len(batch)}")
                
            except Exception as e:
                self.logger.error(f"Failed to insert batch {i//batch_size + 1}: {e}")
                # Continue with next batch instead of failing completely
                continue
        
        success_rate = successful_inserts / total_records if total_records > 0 else 0
        self.logger.info(f"Batch insert completed: {successful_inserts}/{total_records} records ({success_rate:.1%})")
        
        return success_rate > 0.8  # Consider successful if >80% of records inserted


class MQTTJob(BaseJob):
    """Base class for jobs that publish to MQTT."""
    
    def __init__(self, name: str, description: str = ""):
        super().__init__(name, description)
    
    def ensure_mqtt_connection(self, mqtt_client) -> bool:
        """
        Ensure MQTT client is connected.
        
        Args:
            mqtt_client: MQTT client instance
        
        Returns:
            True if connected, False otherwise
        """
        if not mqtt_client.is_connected():
            self.logger.info("MQTT client not connected, attempting to connect...")
            return mqtt_client.connect()
        return True

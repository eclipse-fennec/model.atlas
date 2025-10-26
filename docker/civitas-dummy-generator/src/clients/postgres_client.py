"""PostgreSQL client for database operations."""

import psycopg2
from psycopg2.extras import RealDictCursor
from contextlib import contextmanager
from typing import List, Dict, Any, Optional
from src.config.settings import settings
from src.utils.logging import get_logger

logger = get_logger(__name__)


class PostgreSQLClient:
    """PostgreSQL database client."""
    
    def __init__(self):
        self.host = settings.db_host
        self.port = settings.db_port
        self.user = settings.db_user
        self.password = settings.db_password
    
    def _get_connection_string(self, database: str) -> str:
        """Get connection string for specific database."""
        return f"postgresql://{self.user}:{self.password}@{self.host}:{self.port}/{database}"
    
    @contextmanager
    def get_connection(self, database: str):
        """Get database connection context manager."""
        conn = None
        try:
            conn_string = self._get_connection_string(database)
            conn = psycopg2.connect(conn_string)
            yield conn
        except Exception as e:
            if conn:
                conn.rollback()
            logger.error(f"Database connection error for {database}: {e}")
            raise
        finally:
            if conn:
                conn.close()
    
    def execute_query(self, database: str, query: str, params: Optional[tuple] = None) -> List[Dict[str, Any]]:
        """Execute a SELECT query and return results."""
        try:
            with self.get_connection(database) as conn:
                with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                    cursor.execute(query, params)
                    results = cursor.fetchall()
                    return [dict(row) for row in results]
        except Exception as e:
            logger.error(f"Query execution error: {e}")
            raise
    
    def execute_insert(self, database: str, query: str, params: Optional[tuple] = None) -> Optional[int]:
        """Execute an INSERT query and return the inserted ID if applicable."""
        try:
            with self.get_connection(database) as conn:
                with conn.cursor() as cursor:
                    cursor.execute(query, params)
                    conn.commit()
                    
                    # Try to get the inserted ID if it's an auto-increment field
                    try:
                        return cursor.fetchone()[0] if cursor.description else None
                    except:
                        return None
        except Exception as e:
            logger.error(f"Insert execution error: {e}")
            raise
    
    def execute_batch_insert(self, database: str, query: str, params_list: List[tuple]) -> None:
        """Execute batch INSERT operations."""
        try:
            with self.get_connection(database) as conn:
                with conn.cursor() as cursor:
                    cursor.executemany(query, params_list)
                    conn.commit()
                    logger.info(f"Batch inserted {len(params_list)} records")
        except Exception as e:
            logger.error(f"Batch insert execution error: {e}")
            raise
    
    def insert_meter_reading(self, database: str, meter_id: int, timestamp: str, value: float) -> Optional[int]:
        """Insert a meter reading record."""
        query = """
        INSERT INTO meter_reading (meter_id, timestamp, value)
        VALUES (%s, %s, %s)
        RETURNING id
        """
        return self.execute_insert(database, query, (meter_id, timestamp, value))
    
    def insert_manual_reading(self, database: str, meter_id: int, timestamp: str, value: float) -> Optional[int]:
        """Insert a manual reading record."""
        query = """
        INSERT INTO readings (meter_id, timestamp, value)
        VALUES (%s, %s, %s)
        RETURNING id
        """
        return self.execute_insert(database, query, (meter_id, timestamp, value))
    
    def get_meters(self, database: str) -> List[Dict[str, Any]]:
        """Get all meters from the database."""
        query = "SELECT id, plant_id FROM meter"
        return self.execute_query(database, query)
    
    def create_meter_if_not_exists(self, database: str, plant_id: str) -> int:
        """Create a meter if it doesn't exist and return its ID."""
        # Check if meter exists
        check_query = "SELECT id FROM meter WHERE plant_id = %s"
        existing = self.execute_query(database, check_query, (plant_id,))
        
        if existing:
            return existing[0]['id']
        
        # Create new meter
        insert_query = "INSERT INTO meter (plant_id) VALUES (%s) RETURNING id"
        result = self.execute_insert(database, insert_query, (plant_id,))
        logger.info(f"Created new meter for plant_id: {plant_id}")
        return result
    
    def get_buildings(self, database: str) -> List[Dict[str, Any]]:
        """Get all buildings from the assets database."""
        query = "SELECT id, city, zip, street FROM buildings"
        return self.execute_query(database, query)


# Global client instance
postgres_client = PostgreSQLClient()

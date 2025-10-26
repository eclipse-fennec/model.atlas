"""Setup Buildings job - runs once to populate building and contact data."""

from src.jobs.base import DatabaseJob
from src.clients.postgres_client import postgres_client
from src.config.settings import settings
from src.data_generators.building_data import building_data_generator


class SetupBuildingsJob(DatabaseJob):
    """Job for setting up static building and contact data in the database."""
    
    def __init__(self):
        super().__init__(
            name="setup_buildings",
            description="Setup static building and contact data (runs once)"
        )
        self.database = settings.assets_db
    
    def _execute_delete(self, database: str, query: str, params=None):
        """Execute a DELETE query without trying to fetch results."""
        with postgres_client.get_connection(database) as conn:
            with conn.cursor() as cursor:
                cursor.execute(query, params)
                conn.commit()
                return cursor.rowcount
    
    def execute(self) -> bool:
        """Execute the Setup Buildings job."""
        try:
            # Clear existing data first
            self.logger.info("Clearing existing building and contact data...")
            
            # Delete contacts first (due to foreign key constraint)
            try:
                self._execute_delete(self.database, "DELETE FROM contacts")
                self.logger.info("Cleared existing contacts")
            except Exception as e:
                self.logger.warning(f"Could not clear contacts (table might not exist): {e}")
            
            # Delete buildings
            try:
                self._execute_delete(self.database, "DELETE FROM buildings")
                self.logger.info("Cleared existing buildings")
            except Exception as e:
                self.logger.warning(f"Could not clear buildings (table might not exist): {e}")
            
            # Generate and insert buildings using building_data_generator
            buildings = building_data_generator.generate_buildings()
            building_insert_query = """
            INSERT INTO buildings (id, city, zip, street) 
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                city = EXCLUDED.city,
                zip = EXCLUDED.zip,
                street = EXCLUDED.street
            """
            
            successful_buildings = 0
            for building in buildings:
                try:
                    postgres_client.execute_insert(
                        self.database,
                        building_insert_query,
                        (building["id"], building["city"], building["zip"], building["street"])
                    )
                    successful_buildings += 1
                    self.logger.debug(f"Inserted building {building['id']}: {building['city']}")
                except Exception as e:
                    self.logger.error(f"Failed to insert building {building['id']}: {e}")
            
            self.logger.info(f"Inserted {successful_buildings}/{len(buildings)} buildings")
            
            # Generate and insert contacts using building_data_generator
            building_ids = building_data_generator.get_all_building_ids()
            contacts = building_data_generator.generate_contacts(building_ids)
            contact_insert_query = """
            INSERT INTO contacts (building_id, role, email, phonenumber, first_name, last_name) 
            VALUES (%s, %s, %s, %s, %s, %s)
            ON CONFLICT DO NOTHING
            """
            
            successful_contacts = 0
            for contact in contacts:
                try:
                    postgres_client.execute_insert(
                        self.database,
                        contact_insert_query,
                        (contact["building_id"], contact["role"], contact["email"], 
                         contact["phonenumber"], contact["first_name"], contact["last_name"])
                    )
                    successful_contacts += 1
                    self.logger.debug(f"Inserted contact for building {contact['building_id']}: {contact['first_name']} {contact['last_name']}")
                except Exception as e:
                    self.logger.error(f"Failed to insert contact for building {contact['building_id']}: {e}")
            
            self.logger.info(f"Inserted {successful_contacts}/{len(contacts)} contacts")
            
            # Check success rate
            building_success_rate = successful_buildings / len(buildings) if buildings else 0
            contact_success_rate = successful_contacts / len(contacts) if contacts else 0
            
            overall_success = building_success_rate > 0.8 and contact_success_rate > 0.8
            
            if overall_success:
                self.logger.info(f"Setup Buildings job completed successfully: {successful_buildings} buildings, {successful_contacts} contacts")
            else:
                self.logger.error(f"Setup Buildings job had issues: {successful_buildings}/{len(buildings)} buildings, {successful_contacts}/{len(contacts)} contacts")
            
            return overall_success
            
        except Exception as e:
            self.logger.error(f"Setup Buildings job failed: {e}")
            return False


# Global job instance
setup_buildings_job = SetupBuildingsJob()

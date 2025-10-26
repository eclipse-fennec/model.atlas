"""Main application entry point for the dummy data generator."""

import signal
import sys
import time
from typing import Optional
import click
from apscheduler.schedulers.blocking import BlockingScheduler
from apscheduler.triggers.interval import IntervalTrigger
from apscheduler.executors.pool import ThreadPoolExecutor

from src.config.settings import settings
from src.utils.logging import setup_logging, get_logger

# Import all jobs
from src.jobs.usecase206.excel_import import excel_import_job
from src.jobs.usecase206.operating_data import operating_data_job
from src.jobs.usecase206.remote_meters import remote_meters_job
from src.jobs.usecase206.manual_meters import manual_meters_job
from src.jobs.usecase212.mqtt_stream import mqtt_stream_job
from src.jobs.usecase212.setup_buildings import setup_buildings_job

# Global scheduler instance
scheduler: Optional[BlockingScheduler] = None
logger = None


def setup_scheduler(single_job: Optional[str] = None):
    """Set up the APScheduler with job configurations."""
    global scheduler
    
    # Configure thread pool executor
    executors = {
        'default': ThreadPoolExecutor(max_workers=10)
    }
    
    # Job defaults
    job_defaults = {
        'coalesce': True,  # Combine multiple pending executions into one
        'max_instances': 1,  # Only one instance of each job at a time
        'misfire_grace_time': 30  # Grace time for missed jobs
    }
    
    # Create scheduler
    scheduler = BlockingScheduler(
        executors=executors,
        job_defaults=job_defaults,
        timezone='UTC'
    )
    
    # Job configuration mapping
    job_configs = {
        'excel_import': {
            'func': excel_import_job.run,
            'trigger': IntervalTrigger(seconds=settings.usecase206_interval),
            'name': 'Excel Import Job'
        },
        'operating_data': {
            'func': operating_data_job.run,
            'trigger': IntervalTrigger(seconds=settings.usecase206_interval),
            'name': 'Operating Data Job'
        },
        'remote_meters': {
            'func': remote_meters_job.run,
            'trigger': IntervalTrigger(seconds=1),  # Every second for remote meters
            'name': 'Remote Meters Job'
        },
        'manual_meters': {
            'func': manual_meters_job.run,
            'trigger': IntervalTrigger(seconds=1),  # Every second for manual meters
            'name': 'Manual Meters Job'
        },
        'mqtt_stream': {
            'func': mqtt_stream_job.run,
            'trigger': IntervalTrigger(seconds=settings.usecase212_interval),
            'name': 'MQTT Stream Job'
        },
        'setup_buildings': {
            'func': setup_buildings_job.run,
            'trigger': None,  # Manual trigger only
            'name': 'Setup Buildings Job'
        }
    }
    
    # Add jobs to scheduler
    if single_job:
        # Add only the specified job
        if single_job in job_configs:
            config = job_configs[single_job]
            scheduler.add_job(
                func=config['func'],
                trigger=config['trigger'],
                id=single_job,
                name=config['name'],
                replace_existing=True
            )
            logger.info(f"Scheduler configured with single job: {single_job}")
        else:
            raise ValueError(f"Unknown job: {single_job}")
    else:
        # Add all jobs
        for job_id, config in job_configs.items():
            scheduler.add_job(
                func=config['func'],
                trigger=config['trigger'],
                id=job_id,
                name=config['name'],
                replace_existing=True
            )
        logger.info("Scheduler configured with all jobs")


def signal_handler(signum, frame):
    """Handle shutdown signals gracefully."""
    global scheduler
    logger.info(f"Received signal {signum}, shutting down...")
    
    if scheduler and scheduler.running:
        scheduler.shutdown(wait=True)
    
    sys.exit(0)


@click.group()
@click.option('--log-level', default='INFO', help='Set logging level')
def cli(log_level):
    """Dummy Data Generator CLI."""
    global logger
    
    # Set log level in settings
    settings.log_level = log_level.upper()
    
    # Setup logging
    setup_logging()
    logger = get_logger(__name__)
    
    logger.info("Dummy Data Generator starting...")
    logger.info(f"Configuration: {settings.model_dump()}")


@cli.command()
@click.option('--job', type=click.Choice([
    'excel_import', 'operating_data', 'remote_meters', 'manual_meters', 'mqtt_stream', 'setup_buildings'
]), help='Start scheduling for a specific job only (instead of all jobs)')
def start(job):
    """Start the scheduled job runner."""
    global scheduler, logger
    
    if job:
        logger.info(f"Starting scheduled job runner for job: {job}")
    else:
        logger.info("Starting scheduled job runner for all jobs...")
    
    # Setup signal handlers for graceful shutdown
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    
    try:
        # Setup and start scheduler
        setup_scheduler(single_job=job)
        
        logger.info("Starting scheduler...")
        if job:
            logger.info(f"Only job '{job}' will be scheduled")
        else:
            logger.info(f"Use Case #206 jobs will run every {settings.usecase206_interval} seconds")
            logger.info(f"Use Case #212 job will run every {settings.usecase212_interval} seconds")
            logger.info("Remote and Manual meter jobs will run every 1 second")
        logger.info("Press Ctrl+C to stop")
        
        scheduler.start()
        
    except KeyboardInterrupt:
        logger.info("Received keyboard interrupt, shutting down...")
    except Exception as e:
        logger.error(f"Scheduler failed: {e}")
        sys.exit(1)
    finally:
        if scheduler and scheduler.running:
            scheduler.shutdown(wait=True)
        logger.info("Scheduler stopped")


@cli.command()
@click.option('--job', type=click.Choice([
    'excel_import', 'operating_data', 'remote_meters', 'manual_meters', 'mqtt_stream', 'setup_buildings'
]), help='Run a specific job once')
def run_once(job):
    """Run a specific job once for testing."""
    logger.info(f"Running job '{job}' once...")
    
    job_map = {
        'excel_import': excel_import_job,
        'operating_data': operating_data_job,
        'remote_meters': remote_meters_job,
        'manual_meters': manual_meters_job,
        'mqtt_stream': mqtt_stream_job,
        'setup_buildings': setup_buildings_job
    }
    
    if job in job_map:
        success = job_map[job].run()
        if success:
            logger.info(f"Job '{job}' completed successfully")
        else:
            logger.error(f"Job '{job}' failed")
            sys.exit(1)
    else:
        logger.error(f"Unknown job: {job}")
        sys.exit(1)


@cli.command()
def list_jobs():
    """List all available jobs."""
    jobs = [
        ('excel_import', 'Generate asset master data CSV and upload to MinIO'),
        ('operating_data', 'Generate operating data CSV and upload to MinIO'),
        ('remote_meters', 'Generate remote meter readings and insert to PostgreSQL'),
        ('manual_meters', 'Generate manual meter readings and insert to PostgreSQL'),
        ('mqtt_stream', 'Stream building sensor data to MQTT broker'),
        ('setup_buildings', 'Setup buildings and assets in the database')
    ]
    
    click.echo("Available jobs:")
    for job_name, description in jobs:
        click.echo(f"  {job_name:<15} - {description}")


@cli.command()
def status():
    """Show current configuration and status."""
    click.echo("Dummy Data Generator Status")
    click.echo("=" * 30)
    click.echo(f"Use Case #206 interval: {settings.usecase206_interval} seconds")
    click.echo(f"Use Case #212 interval: {settings.usecase212_interval} seconds")
    click.echo(f"Number of plants: {settings.num_plants}")
    click.echo(f"Number of parts: {settings.num_parts}")
    click.echo(f"Number of buildings: {settings.num_buildings}")
    click.echo(f"Sine wave period: {settings.sine_period} seconds")
    click.echo()
    click.echo("Database connections:")
    click.echo(f"  Host: {settings.db_host}:{settings.db_port}")
    click.echo(f"  Remote meters DB: {settings.remote_meters_db}")
    click.echo(f"  Manual meters DB: {settings.manual_meters_db}")
    click.echo(f"  Manual readings DB: {settings.manual_readings_db}")
    click.echo(f"  Assets DB: {settings.assets_db}")
    click.echo()
    click.echo("MinIO connection:")
    click.echo(f"  Endpoint: {settings.minio_endpoint}")
    click.echo(f"  Bucket: {settings.minio_bucket}")
    click.echo()
    click.echo("MQTT connection:")
    click.echo(f"  Host: {settings.mqtt_host}:{settings.mqtt_port}")
    click.echo(f"  Topic prefix: {settings.mqtt_topic_prefix}")


if __name__ == '__main__':
    cli()

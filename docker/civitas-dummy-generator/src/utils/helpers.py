"""Helper utilities for the dummy data generator."""

import math
import time
from datetime import datetime, timezone
from typing import Any, Dict


def get_current_timestamp() -> datetime:
    """Get current timestamp in UTC."""
    return datetime.now(timezone.utc)


def generate_sinusoidal_value(
    base_value: float = 0.0,
    amplitude: float = 100.0,
    period_seconds: int = 60,
    phase_offset: float = 0.0
) -> float:
    """
    Generate a sinusoidal value based on current time.
    
    Args:
        base_value: Center value around which to oscillate
        amplitude: Maximum deviation from base value
        period_seconds: Period of oscillation in seconds
        phase_offset: Phase offset in radians
    
    Returns:
        Sinusoidal value between (base_value - amplitude) and (base_value + amplitude)
    """
    current_time = time.time()
    angle = (2 * math.pi * current_time / period_seconds) + phase_offset
    return base_value + amplitude * math.sin(angle)


def format_filename_timestamp(dt: datetime = None) -> str:
    """
    Format timestamp for filenames in the format: YYYY_MM_DD_HH:mm
    
    Args:
        dt: Datetime to format, defaults to current time
    
    Returns:
        Formatted timestamp string
    """
    if dt is None:
        dt = get_current_timestamp()
    return dt.strftime("%Y_%m_%d_%H:%M")


def safe_dict_get(data: Dict[str, Any], key: str, default: Any = None) -> Any:
    """Safely get value from dictionary with default."""
    return data.get(key, default)


def increment_counter(counter_file: str, start_value: int = 1) -> int:
    """
    Increment and persist a counter value to a file.
    
    Args:
        counter_file: Path to counter file
        start_value: Starting value if file doesn't exist
    
    Returns:
        Current counter value
    """
    import os
    
    try:
        if os.path.exists(counter_file):
            with open(counter_file, 'r') as f:
                current_value = int(f.read().strip())
        else:
            current_value = start_value - 1
        
        new_value = current_value + 1
        
        # Ensure directory exists
        os.makedirs(os.path.dirname(counter_file), exist_ok=True)
        
        with open(counter_file, 'w') as f:
            f.write(str(new_value))
        
        return new_value
    except Exception:
        return start_value


def generate_plant_id(plant_number: int) -> str:
    """Generate plant ID with 'p_' prefix."""
    return f"p_{plant_number:03d}"


def remove_plant_prefix(plant_id: str) -> str:
    """Remove 'p_' prefix from plant ID."""
    return plant_id.lstrip('p_')

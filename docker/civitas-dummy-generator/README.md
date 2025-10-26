# Dummy Data Generation

Use Case #206

- Source System #1: Excel Import (As CSV)
  - Description
    - Asset masterdata
  - Datastrcuture
    - plant_id: TEXT (starting with p_)
    - city: TEXT
    - zip: NUMBER
    - street: TEXT (street + house number, seperated with a space)
    - type: TEXT NULLABLE
  - Filecontent:
    - 10 Plants (first 10) + 10 parts (last 10), with random data, parts without type
  - Changes:
    - Every 1min, a address changes (house number incremendet)
  - Target:
    - MinIO, one file added every 1min, Filename: basic_data_YYYY_MM_DD_HH:mm
- Source System #2: CSV
  - Description
    - Operating data, relating to assets from source system 1, without p_ prefix
  - Datastrcuture
    - id: NUMBER (incremented per entry)
    - plant_id: TEXT (plant_id mapping to obove plant, without p_)
    - operating_hours: NUMBER
  - Filecontent
    - One entry per plant and part
  - Changes:
    - operating_hours are incremted
    - id is continued from last file
  - Target:
    - MinIO, one file added every 1min, Filename: operating_data_YYYY_MM_DD_HH:mm
- Source System #3: Database
  - Description:
    - Remote meters
  - Datastrcuture
    - Table: meter
      - plant_id: TEXT (plant_id mapping to obove plant, without p_)
      - id: NUMBER (incremented per entry)
    - Table: meter_reading
      - id: NUMBER (incremented per entry)
      - timestamp: TIMESTAMP (2023-01-15 08:00:00)
      - value: NUMBER (-100.0 - 100.0)
      - meter_id: NUMBER (related to meter)
        - Foreign key to meter.id
  - Changes
    - One entry per meter added per second
    - Oscillating in a sinusoidal pattern with a period of 1 minute
  - Target
    - Postgres Database: remote_meters
- Source System #4: Database

  - Description:
    - Manual meters, same as above, but different databses, without relations
  - Datastrcuture
    - Database: manual_meters
      - Table: meter
        - plant_id: TEXT (plant_id mapping to obove plant, without p_)
        - id: NUMBER (incremented per entry)
    - Database: manual_readings
      - Table: readings
        - id: NUMBER (incremented per entry)
        - timestamp: TIMESTAMP (2023-01-15 08:00:00)
        - value: NUMBER (-100.0 - 100.0)
        - meter_id: NUMBER (related to meter)
  - Changes
    - One entry per meter added per second
    - Oscillating in a sinusoidal pattern with a period of 1 minute
  - Target
    - Postgres Database: manual_meters + manual_readings
    - meter + meter_reading (dynamisch ergänzt)

Use Case #212

Static data:

- DB (postgres)
  - Database: assets (with PostgREST)
    - Table: buildings 
        - curl -X GET "http://localhost:3000/buildings"
      - id: NUMBER
      - city: TEXT
      - zip: NUMBER
      - street: TEXT (street + house number, seperated with a space)
    - Table: contacts
        - curl -X GET "http://localhost:3000/contacts"
      - building_id: NUMBER
        - foreign key to buildings.id
      - role: TEXT (Hausmeister)
      - email: TEXT
      - phonenumber: TEXT
      - first_name: TEXT
      - last_name: TEXT
      - last_name: TEXT

Data stream:

- MQTT (mosquitto)
- Python data generator for buildings above:
  - Data structure
    - building_id: TEXT
    - value: NUMBER
    - timestamp: timestamp
  - Changes
    - New reading every 5s per building
    - Oscillating in a sinusoidal pattern with a period of 1 minute
    - Published on mosquitto, not ingested to DB

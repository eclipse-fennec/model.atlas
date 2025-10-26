      CREATE DATABASE usecase212_assets;
      \c usecase212_assets
      -- Table: buildings
      CREATE TABLE buildings (
          id INTEGER PRIMARY KEY,
          city TEXT NOT NULL,
          zip INTEGER NOT NULL,
          street TEXT NOT NULL  -- contains street name and house number
      );

      -- Table: contacts
      CREATE TABLE contacts (
          building_id INTEGER NOT NULL,
          role TEXT NOT NULL,  -- e.g., 'Hausmeister'
          email TEXT,
          phonenumber TEXT,
          first_name TEXT,
          last_name TEXT,
          FOREIGN KEY (building_id) REFERENCES buildings(id)
      );

      CREATE DATABASE usecase206_remote_meters;
      \c usecase206_remote_meters
      -- Table: meter
      CREATE TABLE meter (
          id SERIAL PRIMARY KEY,         -- Auto-incremented ID
          plant_id TEXT NOT NULL         -- Maps to plant_id in another system (no prefix)
      );

      -- Table: meter_reading
      CREATE TABLE meter_reading (
          id SERIAL PRIMARY KEY,         -- Auto-incremented ID
          timestamp TIMESTAMP NOT NULL,  -- e.g., '2023-01-15 08:00:00'
          value NUMERIC NOT NULL CHECK (value BETWEEN -100.0 AND 100.0), -- Range validation
          meter_id INTEGER NOT NULL REFERENCES meter(id)                 -- FK to meter
      );

      CREATE DATABASE usecase206_manual_meters;
      \c usecase206_manual_meters
      -- Table: meter
      CREATE TABLE meter (
          id SERIAL PRIMARY KEY,         -- Auto-incremented ID
          plant_id TEXT NOT NULL         -- Maps to plant_id in another system (no prefix)
      );

      CREATE DATABASE usecase206_manual_readings;
      \c usecase206_manual_readings

      -- Table: readings
      CREATE TABLE readings (
          id SERIAL PRIMARY KEY,         -- Auto-incremented ID
          timestamp TIMESTAMP NOT NULL,  -- e.g., '2023-01-15 08:00:00'
          value NUMERIC NOT NULL CHECK (value BETWEEN -100.0 AND 100.0), -- Range validation
          meter_id INTEGER NOT NULL                -- FK to meter
      );
      
      CREATE DATABASE plant_readings;
      \c plant_readings
      

      GRANT USAGE ON SCHEMA public TO postgres;
      GRANT SELECT ON ALL TABLES IN SCHEMA public TO postgres;

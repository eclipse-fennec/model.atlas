#!/bin/sh
until /usr/bin/mc alias set myminio http://minio:9000 minio minio123; do sleep 1; done &&
/usr/bin/mc mb myminio/dummy-data || true
/usr/bin/mc anonymous set download myminio/dummy-data
/usr/bin/mc anonymous set upload myminio/dummy-data


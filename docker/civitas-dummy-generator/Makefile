# dump dummy-data to dummy-data.sql
dump-dummy-data:
	docker run --rm --network host -v $(PWD)/dump.sql:/dump.sql postgres:16 sh -c 'PGPASSWORD=postgres pg_dump -h localhost -U postgres -d dummy-data' > dummy-data.sql

# dump remote-meters to remote-meters.sql
dump-remote-meters:
	docker run --rm --network host -v $(PWD)/dump.sql:/dump.sql postgres:16 sh -c 'PGPASSWORD=postgres pg_dump -h localhost -U postgres -d remote-meters' > remote-meters.sql

# restore dummy-data from dummy-data.sql
restore-dummy-data:
	docker run --rm --network host -v $(PWD)/dummy-data.sql:/dummy-data.sql postgres:16 sh -c 'PGPASSWORD=postgres psql -h localhost -U postgres -d dummy-data -f /dummy-data.sql'

# restore remote-meters from remote-meters.sql
restore-remote-meters:
	docker run --rm --network host -v $(PWD)/remote-meters.sql:/remote-meters.sql postgres:16 sh -c 'PGPASSWORD=postgres psql -h localhost -U postgres -d remote-meters -f /remote-meters.sql'
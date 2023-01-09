#!/bin/bash
psql -f drop.sql api-test
dropdb --if-exists api-test &&
createdb api-test &&
psql -f ddl.sql api-test &&
psql -f load.sql api-test
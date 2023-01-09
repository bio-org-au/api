#!/bin/bash
JAVA_HOME=/home/mo/.sdkman/candidates/java/11.0.11.hs-adpt/
MICRONAUT_SERVER_PORT=7171
NSL_API_CONFIG_PATH=/home/mo/.nsl/nsl-api-config.groovy
NSL_API_DB_USER=hasura
NSL_API_DB_PWD=hasura
NSL_API_DB_URL=nslapi
NSL_API_DB_SCHEMA=api
#JAVA_OPTS="$JAVA_OPTS -Dmicronaut.config.files=$NSL_API_CONFIG_PATH"
export MICRONAUT_CONFIG_FILES=/home/mo/.nsl/nsl-api-config.groovy
echo $NSL_API_CONFIG_PATH
echo $NSL_API_DB_USER
echo $NSL_API_DB_PWD
echo $NSL_API_DB_URL
echo $NSL_API_DB_SCHEMA
echo $MICRONAUT_CONFIG_FILES
./gradlew run -t
#!/bin/bash
# run IntelliJ_env_var: NSL_API_DB_PWD=hasura;MICRONAUT_SERVER_PORT=7171;NSL_API_DB_SCHEMA=api;JAVA_HOME=/home/mo/.sdkman/candidates/java/11.0.11.hs-adpt/;NSL_API_DB_URL=postgresql://localhost:5432/nslapi;NSL_API_CONFIG_PATH=/home/mo/.nsl/nsl-api-config.groovy;NSL_API_DB_USER=hasura
# test IntelliJ_env_var: NSL_API_DB_PWD=hasura;NSL_API_CONFIG_PATH=src/test/resources/nsl-api-config.groovy;NSL_API_DB_URL=postgresql://127.0.0.1:5432/nslapi;NSL_API_DB_USER=hasura;NSL_API_DB_SCHEMA=api
JAVA_HOME=/home/mo/.sdkman/candidates/java/11.0.11.hs-adpt/
MICRONAUT_SERVER_PORT=7171
NSLAPICONFIGPATH='src/test/resources/nsl-api-config.groovy'
NSLAPIDBUSER=hasura
NSLAPIDBPWD=hasura
NSLAPIDBURL='postgresql://localhost:5432/nslapi'
NSLAPIDBSCHEMA=api

JAVA_OPTS="-Xmx64m -XX:MaxPermSize=64m -Dmicronaut.config.files=$NSLAPICONFIGPATH"

echo $NSLAPICONFIGPATH
echo $NSLAPIDBUSER
echo $NSLAPIDBPWD
echo $NSLAPIDBURL
echo $NSLAPIDBSCHEMA
./gradlew test
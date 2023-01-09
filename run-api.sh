#!/bin/bash
export NSLAPISERVERPORT=8095
for process in $(netstat -peanut | grep LISTEN | grep $NSLAPISERVERPORT | awk '{print $9}' | sed 's/\/java//g');
do
  echo "Killing ps $item"
  kill -9 $item;
done
export MICRONAUT_ENVIRONMENTS=dev
export NSLAPICONFIGPATH=/home/mo/.nsl/nsl-api-config.groovy
export NSLAPIDBUSER=hasura
export NSLAPIDBPWD=hasura
export NSLAPIDBNAME=nslapi
export NSLAPIDBURL="postgresql://localhost:5432/"$NSLAPIDBNAME
export NSLAPIDBSCHEMA=api
versionNumber=$(cat build.gradle | grep '^version "' | sed 's/version \"//g' | sed 's/\"//g')
echo "Building NSL API: v"$versionNumber
./gradlew clean assemble
echo "Running version: "$versionNumber
JARFILE="build/libs/nslapi-"$versionNumber"-all.jar"
echo $NSLAPICONFIGPATH
echo "DB in script: $NSLAPIDBURL u:$NSLAPIDBUSER p: $NSLAPIDBPWD s: $NSLAPIDBSCHEMA"

#./gradlew run -t
if [ ! -f $jarfilename ]; then
  echo "you need mapper-*.jar in build/libs/"
  exit 1
fi
java -version
java \
-XX:+UnlockExperimentalVMOptions -Dcom.sun.management.jmxremote -noverify \
-Dmicronaut.config.files=${NSLAPICONFIGPATH} -Dmicronaut.server.port=${NSLAPISERVERPORT} \
-jar ${JARFILE}

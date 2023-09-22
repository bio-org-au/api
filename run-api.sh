#!/bin/bash
#!/bin/bash
if type -p java; then
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    _java="$JAVA_HOME/bin/java"
else
    echo "Java not found. use sdkman to install java 11"
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo version "$version"
    major_version="${version%%.*}"
    if [[ "major_version" -ne "11" ]]; then
        echo "You need java version 11 to run the project"
        exit 1
    else
        echo "Compatible version of java found"
    fi
fi

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
versionNumber=$(cat version.properties  | grep 'version.semver=' | sed 's/version.semver=//g')
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

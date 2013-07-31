#!/bin/bash
export JAVA_OPTS="$JAVA_OPTS -Xmx1024 -XX:MaxPermSize=512m"
export MAVEN_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"
mvn clean
mvn package
mvn hpi:run

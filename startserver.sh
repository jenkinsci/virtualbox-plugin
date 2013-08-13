#!/bin/bash
sudo launchctl unload /Library/LaunchDaemons/org.jenkins-ci.plist
export JAVA_OPTS="$JAVA_OPTS -Xmx1024 -XX:MaxPermSize=512m"
export MAVEN_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"
cd plugin
mvn clean
mvn install
mvn hpi:run

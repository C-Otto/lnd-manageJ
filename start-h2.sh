#!/bin/bash
if [ "x$TERM" == "xdumb" ];
then
  export TERM="xterm"
fi
./gradlew :application:bootJar && clear && java -Dspring.profiles.active=h2 -jar application/build/libs/application-boot.jar

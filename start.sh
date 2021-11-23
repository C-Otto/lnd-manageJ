#!/bin/bash
if [ "x$TERM" == "x" ];
then
  export TERM="xterm"
fi
./gradlew application:bootJar && clear && java -jar application/build/libs/application-boot.jar

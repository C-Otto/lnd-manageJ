#!/bin/bash
if [ "x$TERM" == "xdumb" ];
then
  export TERM="xterm"
fi
./gradlew ui-demo:bootRun

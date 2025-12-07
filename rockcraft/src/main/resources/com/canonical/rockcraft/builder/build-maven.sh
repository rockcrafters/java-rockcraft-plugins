#!/bin/sh -e
WORKDIR=${1:-`pwd`}
if [ $# -gt 0 ]; then
  shift
fi
if [ $# -eq 0 ]; then
  GOAL=!!goal!!
else
  GOAL=$@
fi
(cd ${WORKDIR} && /usr/share/maven/bin/mvn -o --settings /home/ubuntu/.m2/settings.xml $GOAL)

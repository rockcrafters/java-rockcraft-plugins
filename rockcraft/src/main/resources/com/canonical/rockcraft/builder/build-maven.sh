#!/bin/sh -e
WORKDIR=${1:-`pwd`}
if [ $# -gt 0 ]; then
  shift
fi
if [ $# -eq 0 ]; then
  GOAL=package
else
  GOAL=$@
fi
export HOME=/home/builder
(cd $WORKDIR && /usr/share/maven/mvn $GOAL)

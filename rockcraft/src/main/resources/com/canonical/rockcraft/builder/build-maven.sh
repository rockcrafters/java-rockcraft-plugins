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
(cd $WORKDIR && mvn $GOAL)

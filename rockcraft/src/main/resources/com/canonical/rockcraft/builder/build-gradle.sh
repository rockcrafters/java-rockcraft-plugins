#!/bin/sh -e
WORKDIR=${1:-`pwd`}
if [ $# -gt 0 ]; then
    shift
fi
if [ $# -eq 0 ]; then
    TASK=build -x checkRockcraft
else
    TASK=$@
fi
export HOME=/var/lib/pebble/default
export GRADLE_USER_HOME=${GRADLE_USER_HOME:-${WORKDIR}/.gradle}
(cd ${WORKDIR} && gradle $TASK)

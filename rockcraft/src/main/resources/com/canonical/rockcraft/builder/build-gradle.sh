#!/bin/sh -e
WORKDIR=${1:-`pwd`}
if [ $# -gt 0 ]; then
    shift
fi
if [ $# -eq 0 ]; then
    TASK="build -x checkRockcraft"
else
    TASK=$@
fi
(cd ${WORKDIR} && gradle $TASK)

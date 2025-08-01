#!/bin/sh -e
WORKDIR=${1:-`pwd`}
if [ $# -gt 0 ]; then
    shift
fi
if [ $# -eq 0 ]; then
    TASK="!!goal!! -x checkRockcraft"
else
    TASK=$@
fi
export GRAALVM_HOME=/graalvm-ce/
!!gradle-user-home!!
(cd ${WORKDIR} && gradle nativeCompile --no-daemon $TASK)

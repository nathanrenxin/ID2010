#!
# 2018-08-20/fki Checked for version 13
# -- ----------------------------------------------------------------
# -- This file is for Unix/Linux systems.
# -- This file starts a Dexter from its remote installation point.
# -- ----------------------------------------------------------------

SCRIPT_HOME=$(dirname $0)

LABROOT=${SCRIPT_HOME}/../..

PCY=${LABROOT}/lib/policy.all

LIB=${LABROOT}/lib

JRN=${LIB}/JarRunner.jar

CFG=${SCRIPT_HOME}/httpd.cfg

if [ -a $CFG ]; then
    . $CFG
    HTTP=$CODEBASE
fi

CBS=${HTTP}/Dexter.jar

JAR=${HTTP}/Dexter.jar

#echo "CBS=$CBS"
#echo "JAR=$JAR"

unset CLASSPATH

CBO=-Djava.rmi.server.useCodebaseOnly=false

java $CBO -Djava.security.policy=$PCY -Djava.rmi.server.codebase="$CBS" -jar $JRN $JAR $*

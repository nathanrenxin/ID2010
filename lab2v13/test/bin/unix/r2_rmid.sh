#!
# 2018-08-20/fki Checked for version 13
# -- ----------------------------------------------------------------
# -- This file is for Unix/Linux systems.
# -- This file starts the rmi deamon
# -- ----------------------------------------------------------------

SCRIPT_HOME=$(dirname $0)

LABROOT=${SCRIPT_HOME}/../..

LOG=/var/tmp/rmid_log

PCY=${LABROOT}/lib/policy.all

echo "********************************"
echo "Starting the Java RMI daemon"
echo "You can shut it down by giving the command    rmid -stop"
echo "in a command window. That will also stop activated services."
echo "LOG = $LOG"
echo "PCY = $PCY"

if [ -d $LOG ]; then
    echo "Removing $LOG"
    rm -r $LOG
fi

exec rmid -log $LOG -J-Djava.security.policy=$PCY -J-Djava.rmi.server.logCalls=true

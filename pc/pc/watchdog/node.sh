#!/bin/bash

# example crontab row for executing script every 10 minutes
# */10 * * * * /home/otjarvin/src/socos/trunk/pc/watchdog/node.sh

SOCOS="/home/otjarvin/src/socos/trunk/pc"
PATH="/home/otjarvin/python/bin:/home/otjarvin/apache2/bin:/home/otjarvin/pvs:/home/otjarvin/pvs/yices-1.0.34/bin:$PATH"
PVS_LIBRARY_PATH="/home/otjarvin/pvs/nasalib"

MAIL="otjarvin@abo.fi"

APACHE="/home/otjarvin/apache2/bin/httpd"
DATADIR="/localhome/otjarvin/server"
MODULEDIR="/home/otjarvin/apache2/modules"
DEFERHOSTS=asg5:8081,asg6:8081,asg7:8081,asg8:8081

ATTEMPTS=60

IBPFILES="$SOCOS/acceptancetests/basic/flag.ibp"

########################################################################

if [ "$1" =  "master" ]; then
   CHECKSERVER="$SOCOS/check_server --module-dir=$MODULEDIR --apache=$APACHE --data-dir=$DATADIR --max-processes=0 --defer-hosts=$DEFERHOSTS"
else
   CHECKSERVER="$SOCOS/check_server --module-dir=$MODULEDIR --apache=$APACHE --data-dir=$DATADIR --max-processes=auto"
fi

########################################################################

$SOCOS/remote_socos --plain --attempts=$ATTEMPTS $IBPFILES

if [ $? -eq 0 ]; then
   echo node.sh: success
else
   echo node.sh: failure
   tail $DATADIR/error.log | mailx -n -s "watchdog test on `hostname` failed" $MAIL
   $CHECKSERVER stop
   $CHECKSERVER start
fi


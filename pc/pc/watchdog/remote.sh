#!/bin/bash

SOCOS="/home/otjarvin/code/socos/trunk/pc"
MAIL="otjarvin@abo.fi"
HOST="imped.abo.fi:8082"

IBPFILES="$SOCOS/acceptancetests/basic/*.ibp"

########################################################################

while [ 1 ]
do
   $SOCOS/remote_socos --plain --host $HOST $IBPFILES

   if [ $? -eq 0 ]; then
      echo remote.sh: success
   else
      echo remote.sh: failure
      echo "remote.sh failed on " `hostname` | mailx -n -s "node test failed" $MAIL
   fi

   sleep 600
done

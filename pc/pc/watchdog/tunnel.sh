#!/bin/bash

# example crontab row for executing script every minute
# */1 * * * * /home/joheriks/wip/socos/trunk/pc/watchdog/tunnel.sh

SOCOS=/home/joheriks/wip/socos/trunk/pc
SOURCEPORT=8082
TARGETPORT=8082
TARGETHOST=asterope.abo.fi
MAIL=joheriks@abo.fi

$SOCOS/watchdog/portavail $SOURCEPORT

if [ $? -eq 0 ]; then
   ssh -L :$SOURCEPORT:$TARGETHOST:$TARGETPORT $TARGETHOST -N &
   echo "tunnel restarted"
   echo "topic says it all" | mailx -n -s "tunnel restarted" $MAIL
fi

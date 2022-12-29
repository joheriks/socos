# Configuration file for checking server
import init_socos

# Server id. A unique string must be used for each node, otherwise
# deferral loops will occur. If None, generate an id.
ID = None

# Expose debugging information on the web interface
DEBUG = True

# Directory in which to store logs and data; if None, use
# $SOCOS_DIR/server/data_$UID
DATA_DIR = None

# Port listening for incoming connections
PORT = 8081

# User and group ids; None = use effective uid and gid
USER = None
GROUP = None

# Max number of concurrent processes. If more are running,
# checking requests are deferred to other hosts, or if no
# available hosts, denied. Can be zero. If None, the number
# of processes in not limited; if "auto", set to number of
# CPU cores.
MAX_PROCESSES = None

# Hosts to which checking should be deferred if there are no
# free processes.
DEFER_HOSTS = []

# Flag to force validation of user id.
VALIDATE_USER = False

# Flag for secure connection
SSL = False

# Port listening for incoming secure connections
SSL_PORT = 8881

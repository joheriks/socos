#!/bin/sh

# run all tests, but indicate error if any one test failes
ERRORLEVEL=0
export PYTHONPATH=`python init_socos.py`
cd unittests
for f in *.py; do
    python ${f};
    if [ $? -ne 0 ]; then
        ERRORLEVEL=1
    fi
done
exit $ERRORLEVEL

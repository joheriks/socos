#!/bin/sh
./check_server --data-dir=/tmp/s1 --max-processes=0 --port=8081 start
./check_server --data-dir=/tmp/s2 --port=8082 start
./check_server --data-dir=/tmp/s3 --port=8083 start

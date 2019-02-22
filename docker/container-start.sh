#!/bin/sh
DATE=`date +%Y-%m-%d`
exec java -server -jar app.jar -e $1 | tee -a server_logs/workers_${DATE}.log

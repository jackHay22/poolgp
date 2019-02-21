#!/bin/sh

exec java -server -jar app.jar -e $1 | tee -a server_logs/workers.log

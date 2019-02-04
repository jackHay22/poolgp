#!/bin/sh

#NOTE: uses sumologic to aggregate logs

# arg: (1) task definition
# arg: (2) sumologic log endpt

# redirect_logs() {
#   while read LINE
#   do
#     echo $LINE
#     curl -G $1 \
#     --data-urlencode "log=${LINE}" > /dev/null 2>&1
#   done
# }

exec java -jar app.jar -e $1 #| redirect_logs $2

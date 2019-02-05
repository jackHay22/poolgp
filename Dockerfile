FROM hypriot/rpi-java:jre-1.8.111
#FROM java:8-jre-alpine

MAINTAINER Jack Hay "https://github.com/jackHay22"

ARG TASK_DEFINITION

ADD target/uberjar/poolgp-*.*.*-SNAPSHOT-standalone.jar app.jar

ADD docker/container-start.sh /run.sh
#RUN chmod a+x /run.sh

ADD $TASK_DEFINITION task_definition.json

CMD /run.sh task_definition.json
#$LOG_ENDPT

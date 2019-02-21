FROM java:8-jre-alpine

MAINTAINER Jack Hay "https://github.com/jackHay22"

ARG TASK_DEFINITION

VOLUME /server_logs

ADD target/uberjar/poolgp-*-standalone.jar app.jar

ADD docker/container-start.sh /run.sh

ADD $TASK_DEFINITION task_definition.json

CMD /run.sh task_definition.json

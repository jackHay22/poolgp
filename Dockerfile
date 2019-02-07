FROM java:8-jre-alpine
#FROM hypriot/rpi-java:jre-1.8.111

MAINTAINER Jack Hay "https://github.com/jackHay22"

ARG TASK_DEFINITION

ADD target/uberjar/poolgp-*-standalone.jar app.jar

ADD docker/container-start.sh /run.sh

ADD $TASK_DEFINITION task_definition.json

CMD /run.sh task_definition.json

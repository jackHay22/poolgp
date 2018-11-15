FROM java:8-jre-alpine

MAINTAINER Jack Hay "https://github.com/jackHay22"

ENV SERVER_PORT=4000

ADD target/uberjar/poolgp-*.*.*-SNAPSHOT-standalone.jar app.jar

ADD docker/container-start.sh /run.sh
RUN chmod a+x /run.sh

CMD /run.sh $SERVER_PORT

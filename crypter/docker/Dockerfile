FROM azul/zulu-openjdk-alpine:11.0.7-jre

ARG BUNDLE_NAME
ARG BUNDLE_VERSION

WORKDIR /financemanager

COPY ./target/$BUNDLE_NAME-$BUNDLE_VERSION.jar .
COPY ./docker/entrypoint.sh .
COPY ./docker/entrypoint_debug.sh .

RUN chmod u+x *.sh

ENV JAR_FILE=$BUNDLE_NAME-$BUNDLE_VERSION.jar

ENTRYPOINT ["/financemanager/entrypoint.sh"]
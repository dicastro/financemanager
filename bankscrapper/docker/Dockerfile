FROM azul/zulu-openjdk-alpine:11.0.7-jre

ARG BUNDLE_NAME
ARG BUNDLE_VERSION

RUN apk add tesseract-ocr

WORKDIR /financemanager

COPY ./target/$BUNDLE_NAME-$BUNDLE_VERSION.jar .
COPY ./docker/entrypoint.sh .
COPY ./docker/entrypoint_debug.sh .

RUN chmod u+x *.sh

ENV JAR_FILE=$BUNDLE_NAME-$BUNDLE_VERSION.jar

VOLUME /financemanager/dbfiles
VOLUME /financemanager/bankscrapper

ENTRYPOINT ["/financemanager/entrypoint.sh"]
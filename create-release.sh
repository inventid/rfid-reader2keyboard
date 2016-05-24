#!/bin/bash

# Fail hard on ANY error here!
set -e

export BUILD_DATE=`date` && \
   export JSON="{\"tag_name\": \"v${BUILD_NUMBER}\",\"name\":\"Build ${BUILD_NUMBER}\",\"body\":\"Build ${BUILD_NUMBER} of ${BUILD_DATE} (${COMMIT})\"}" && \
   echo $JSON && \
   export DATA=`curl -s -POST -u inventid-deploy:$GITHUB_PASS https://api.github.com/repos/inventid/rfid-reader2keyboard/releases -d "$JSON" -H "Content-Type: application/json" -H "Accept: application/json"` && \
   echo $DATA && \
   export RELEASE_ID=`echo $DATA | jq '.id'` && \
   export UPLOAD_URL=`echo $DATA | jq '.upload_url' | sed s/\{\?name,label\}/\?name=rfid-reader2keyboard.jar/g | sed s/\"//g` && \
   echo $RELEASE_ID && \
   curl -s -POST -u inventid-deploy:$GITHUB_PASS $UPLOAD_URL -H "Content-Type: application/java-archive" --data-binary @target/rfid-reader2keyboard-1.0-SNAPSHOT-jar-with-dependencies.jar


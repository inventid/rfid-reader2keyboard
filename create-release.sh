#!/bin/bash

export BUILD_DATE=`date` && \
   export DATA=`curl -POST -u inventid-deploy:$GITHUB_PASS https://api.github.com/repos/inventid/rfid-reader2keyboard/releases -d "{\"tag_name\": \"v${BUILD_NUMBER}\",\"name\":\"Build of ${BUILD_DATE} (${COMMIT})\"}" -H "Content-Type: application/json" -H "Accept: application/json"` && \
   export RELEASE_ID=`echo $DATA | jq '.id'` && \
   export UPLOAD_URL=`echo $DATA | jq '.upload_url' | sed s/\{\?name\}/\?name=rfid-reader2keyboard.jar/g | sed s/\"//g` && \
   echo $RELEASE_ID && \
   curl -POST -u inventid-deploy:$GITHUB_PASS $UPLOAD_URL -H "Content-Type: application/java-archive" --data-binary @target/rfid-reader2keyboard-1.0-SNAPSHOT.jar

env

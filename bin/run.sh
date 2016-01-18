#!/bin/sh
nohup java -Dwaltz.port=8080 -Dwaltz.ssl.enabled=false -jar ../waltz-web/target/uber-waltz-web-1.0-SNAPSHOT.jar &
nohup java -jar ../waltz-web/target/uber-waltz-web-1.0-SNAPSHOT.jar &
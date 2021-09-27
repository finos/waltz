#!/bin/bash

# Set default values for required env vars
export DB_HOST=${DB_HOST:-"postgres"}
export DB_PORT=${DB_PORT:-"5432"}
export DB_NAME=${DB_NAME:-"waltz"}
export DB_USER=${DB_USER:-"waltz"}
export DB_PASSWORD=${DB_PASSWORD:-"waltz"}
export DB_SCHEME=${DB_SCHEME:-"waltz"}
export WALTZ_FROM_EMAIL=${WALTZ_FROM_EMAIL:-"help@finos.org"}
export WALTZ_BASE_URL=${WALTZ_BASE_URL:-"http://127.0.0.1:8080/"}
export CHANGELOG_FILE=${CHANGELOG_FILE:-"/opt/waltz/liquibase/db.changelog-master.xml"}

db_action () {
    while [[ $(pg_isready --host="${DB_HOST}" --port="${DB_PORT}" --username="${DB_USER}" --dbname="${DB_NAME}") != *"accepting connections"* ]]
    do
        echo ">>> Database is not ready yet."
        sleep 5s
    done
    echo ">>> Database is ready."

    # changeLogFile must be relative path
    liquibase --changeLogFile=../../../${CHANGELOG_FILE} --hub-mode=off --username="${DB_USER}" --password="${DB_PASSWORD}" --url=jdbc:postgresql://"${DB_HOST}":"${DB_PORT}"/"${DB_NAME}" "$1"
    if [ $? -eq 0 ]; then
        echo ">>> Database updated."
    else
        echo ">>> Database failed to update."
        exit 1
    fi
}

run_waltz () {
    envsubst < /home/waltz/.waltz/waltz-template > /home/waltz/.waltz/waltz.properties
    catalina.sh run
}

while [[ "$#" -gt 0 ]]; do
    case $1 in
        update) echo ">>> Update DB" && db_action update ;;
        run) echo ">>> Run Waltz" && run_waltz ;;
    esac
    shift
done
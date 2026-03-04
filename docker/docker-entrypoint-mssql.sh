#!/bin/bash

# Set default values for required env vars
export DB_HOST=${DB_HOST:-"sqlserver"}
export DB_PORT=${DB_PORT:-"1433"}
export DB_NAME=${DB_NAME:-"waltz"}
export DB_USER=${DB_USER:-"sa"}
export DB_PASSWORD=${DB_PASSWORD:-"Waltz#123"}
export DB_SCHEME=${DB_SCHEME:-"dbo"}
export WALTZ_FROM_EMAIL=${WALTZ_FROM_EMAIL:-"help@finos.org"}
export WALTZ_BASE_URL=${WALTZ_BASE_URL:-"http://127.0.0.1:8080/"}
export CHANGELOG_FILE=${CHANGELOG_FILE:-"/opt/waltz/liquibase/db.changelog-master.xml"}

db_action () {
    while ! /opt/mssql-tools18/bin/sqlcmd -C -S "${DB_HOST},${DB_PORT}" -U "${DB_USER}" -P "${DB_PASSWORD}" -Q "SELECT 1" >/dev/null 2>&1
    do
        echo ">>> Database is not ready yet."
        sleep 5s
    done
    echo ">>> Database is ready."

    /opt/mssql-tools18/bin/sqlcmd -C -S "${DB_HOST},${DB_PORT}" -d master -U "${DB_USER}" -P "${DB_PASSWORD}" -Q "IF DB_ID('${DB_NAME}') IS NULL CREATE DATABASE [${DB_NAME}]" >/dev/null 2>&1

    # changeLogFile must be relative path
    liquibase --changeLogFile=../../../${CHANGELOG_FILE} --hub-mode=off --username="${DB_USER}" --password="${DB_PASSWORD}" --url="jdbc:sqlserver://${DB_HOST}:${DB_PORT};databaseName=${DB_NAME};encrypt=true;trustServerCertificate=true" "$1"
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

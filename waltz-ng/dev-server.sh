#!/bin/sh
until npm run dev-server; do
  echo "Respawmning dev server as crashed with $? " >&2
  sleep 1 
done 

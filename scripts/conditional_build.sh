#!/bin/bash

if [ ! -f "$1" ]; then
  echo "Service JAR not found, building now..."
  /app/gradlew assemble
fi

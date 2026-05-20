#!/bin/bash
set -e

DEVICES=$(adb devices | grep -w "device" | grep -v "List" | awk '{print $1}')

if [ -z "$DEVICES" ]; then
  echo "No devices connected."
  exit 1
fi

echo "Building debug APK..."
./gradlew assembleDebug

APK="app/build/outputs/apk/debug/app-debug.apk"

for DEVICE in $DEVICES; do
  echo "Installing on $DEVICE..."
  adb -s "$DEVICE" install -r "$APK" && echo "  Done: $DEVICE" || echo "  Failed: $DEVICE"
done

echo "Installed on $(echo "$DEVICES" | wc -l | tr -d ' ') device(s)."

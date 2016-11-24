#!/bin/bash

# Fix the CircleCI path
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$PATH"

echo "ANDROID_HOME=$ANDROID_HOME"

#DEPS="$ANDROID_HOME/installed-dependencies"

#if [ ! -e $DEPS ]; then
  echo y | android update sdk -u -a -t platform-tools &&
  echo y | android update sdk -u -a -t android-25 &&
  echo y | android update sdk -u -a -t tool &&

  echo y | android update sdk -u -a -t build-tools-25.0.1 &&

  echo y | android update sdk -u -a -t extra-android-support &&
  echo y | android update sdk -u -a -t extra-google-google_play_services &&
  echo y | android update sdk -u -a -t extra-google-m2repository &&
  echo y | android update sdk -u -a -t extra-android-m2repository &&
  echo y | android update sdk -u -a -t extra-google-play_billing #&&
#  touch $DEPS
#fi


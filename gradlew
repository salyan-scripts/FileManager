#!/bin/sh
APP_HOME=$(pwd)
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec java -Xmx64m -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

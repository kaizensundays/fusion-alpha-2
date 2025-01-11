#!/bin/bash

export JAVA_HOME="$JAVA_17_HOME"

export MAVEN_OPTS="-Xmx1g"

mvn clean install -P test

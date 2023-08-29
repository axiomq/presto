#!/bin/bash

export CONTINUOUS_INTEGRATION=true
export MAVEN_OPTS="-Xmx1024M -XX:+ExitOnOutOfMemoryError"
export MAVEN_INSTALL_OPTS="-Xmx2G -XX:+ExitOnOutOfMemoryError"
export RETRY=.github/bin/retry

export JAVA_HOME=/usr/lib/jvm/jdk1.8.0_333
export PATH=$JAVA_HOME/bin:$PATH

echo "..... ..... ..... Maven Checks"
export MAVEN_OPTS="${MAVEN_INSTALL_OPTS}"
#./mvnw install -B -V -T C1 -DskipTests -P ci -pl '!:presto-server-rpm,!presto-test-coverage'
#./mvnw install -B -V -T C1 -DskipTests -P ci -pl '!:presto-server-rpm,!presto-test-coverage,!presto-docs'
./mvnw install -B -V -T C1 -DskipTests -P ci -pl '!:presto-test-coverage,!presto-docs'
#./mvnw install -B -V -T C1 -DskipTests -P ci -pl '!presto-test-coverage'
echo "..... ..... ..... Clean Maven Output"
run: ./mvnw clean -pl '!:presto-server,!:presto-cli,!presto-test-coverage'
echo "..... ..... ..... "

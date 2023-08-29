#!/bin/bash

export CONTINUOUS_INTEGRATION=true
export MAVEN_OPTS="-Xmx1024M -XX:+ExitOnOutOfMemoryError"
export MAVEN_INSTALL_OPTS="-Xmx2G -XX:+ExitOnOutOfMemoryError"
export MAVEN_FAST_INSTALL="-B -V --quiet -T C1 -DskipTests -Dair.check.skip-all -Dmaven.javadoc.skip=true"
export MAVEN_TEST="-B -Dair.check.skip-all -Dmaven.javadoc.skip=true -DLogTestDurationListener.enabled=true --fail-at-end"
export RETRY=.github/bin/retry

export JAVA_HOME=/usr/lib/jvm/jdk1.8.0_333
export PATH=$JAVA_HOME/bin:$PATH

#presto-resource-group-managers

echo "..... ..... ..... maven check"
# export MAVEN_OPTS="${MAVEN_FAST_INSTALL}"
#./mvnw clean install -DskipTests -Dmaven.javadoc.skip=true -Dair.check.skip-all=true -T C1 -pl '!presto-docs,!presto-server-rpm'
./mvnw clean install -DskipTests -Dmaven.javadoc.skip=true -Dair.check.skip-all=true -T C1 -pl '!:presto-test-coverage,!presto-docs'
echo "..... ..... ..... "

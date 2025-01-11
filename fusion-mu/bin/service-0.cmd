@echo off

set JAVA_HOME=%JAVA_17_HOME%

set IGNITE_UPDATE_NOTIFIER=false

set IGNITE_WORK_DIR=%cd%\ignite\node0

echo "IGNITE_WORK_DIR=%IGNITE_WORK_DIR%"

set IGNITE_SKIP_CONFIGURATION_CONSISTENCY_CHECK=true

if "%JAVA_HOME%"=="%JAVA_17_HOME%" (

echo "Ok"

set JAVA_OPTS= ^
 --add-opens=java.base/jdk.internal.access=ALL-UNNAMED ^
 --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED ^
 --add-opens=java.base/sun.nio.ch=ALL-UNNAMED ^
 --add-opens=java.base/sun.util.calendar=ALL-UNNAMED ^
 --add-opens=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED ^
 --add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED ^
 --add-opens=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED ^
 --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED ^
 --add-opens=java.base/java.io=ALL-UNNAMED ^
 --add-opens=java.base/java.nio=ALL-UNNAMED ^
 --add-opens=java.base/java.net=ALL-UNNAMED ^
 --add-opens=java.base/java.util=ALL-UNNAMED ^
 --add-opens=java.base/java.util.concurrent=ALL-UNNAMED ^
 --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED ^
 --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED ^
 --add-opens=java.base/java.lang=ALL-UNNAMED ^
 --add-opens=java.base/java.lang.invoke=ALL-UNNAMED ^
 --add-opens=java.base/java.math=ALL-UNNAMED ^
 --add-opens=java.sql/java.sql=ALL-UNNAMED ^
 --add-opens=java.base/java.lang.reflect=ALL-UNNAMED ^
 --add-opens=java.base/java.time=ALL-UNNAMED ^
 --add-opens=java.base/java.text=ALL-UNNAMED ^
 --add-opens=java.management/sun.management=ALL-UNNAMED ^
 --add-opens=java.desktop/java.awt.font=ALL-UNNAMED
)

start "Node" %JAVA_HOME%/bin/java -Xmx256m ^
	-Dlogging.config=log4j2-0.xml ^
	-Dlog4j.shutdownHookEnabled=false ^
	-Dserver.port=7701 ^
        -Dspring.profiles.active=dev ^
	-Djava.net.preferIPv4Stack=true ^
	-Dreactor.netty.ioWorkerCount=4 ^
	-Dreactor.schedulers.defaultPoolSize=4 ^
        -Dcluster.votes=2 ^
        -Dcluster.quorum=2 ^
	%JAVA_OPTS% ^
	-jar service.jar

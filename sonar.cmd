
set JAVA_HOME="%JAVA_17_HOME%"

mvn verify sonar:sonar %SONAR_OPTS_FUSION_ALPHA% -Dsonar.branch.name=dev -P sonar

mvn clean install -DskipTests
cd jhotdraw-samples/jhotdraw-samples-misc
mvn exec:java "-Dexec.mainClass=org.jhotdraw.samples.svg.Main"
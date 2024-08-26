rm target/classes/com/qrypt/*;
mvn compile;
jar cf target/classes/com/qrypt/QryptProvider.jar -C /workspaces/java-random-provider/random-provider/target/classes/com/qrypt QryptProvider.class;
sudo cp target/classes/com/qrypt/QryptProvider.jar /docker-java-home/lib/;
sudo cp target/classes/com/qrypt/QryptProvider.jar /home/vscode/.vscode-server/extensions/redhat.java-1.33.0-linux-arm64/jre/17.0.11-linux-aarch64/lib/;

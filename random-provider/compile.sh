mvn compile;
rm target/classes/com/qrypt/*.jar;
jar cf target/classes/com/qrypt/QRNGRandomProvider.jar -C /workspaces/java-random-provider/random-provider/target/classes/com/qrypt QRNGRandomProvider.class;

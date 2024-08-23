# Helpful links
(GetRandom Provide Example - Github)[https://github.com/marschall/getrandom-provider]

# Update Java Security File
`sudo vim /docker-java-home/conf/security/java.security`
Add this line: `security.provider.1=com.qrypt.QRNGRandomProvider`
Bump the rest of the providers down in priority

# Build
From the random-provider folder:
mvn compile;
rm target/classes/com/qrypt/*.jar;
jar cf target/classes/com/qrypt/QRNGRandomProvider.jar -C /workspaces/java-random-provider/random-provider/target/classes/com/qrypt QRNGRandomProvider.class;

# Run
From the random-provider folder:
cd target/classes;
java com.qrypt.Main;



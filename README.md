## Helpful links
* (GetRandom Provide Example - Github)[https://github.com/marschall/getrandom-provider]
* EJBCA github project: https://github.com/Keyfactor/ejbca-ce
* WildFly 26.1.3 AppServer (https://www.wildfly.org/)


## Minimum Requirements
Install the following tools in your dev/test environment
* JDK 11 (Preferably OpenJDK)
* Maven 3.x
* Docker runtime
* 4-8GB available RAM. Closer to 6-8GB if testing with WildFly server

Optionally (for local appserver testing)
* WildFly 26.1.3 AppServer


## Build
From the root project folder please execute:
`mvn clean install`
This command builds the following artifacts:
* random-provider-1.0-SNAPSHOT.jar - the actual Security Provider impl for SecureRandom operations
* random-provider-initializer.war - a webapp that's meant to initialize Qrypt provider in AppServer environment
* SampleRandomDemo.war - an optional webapp to demo SecureRandom 
* qrypt/ejbca-ce:1.0-SNAPSHOT - a custom docker image based off keyfactor/ejbca that includes qrypt random provider

## Execute
For simple no-client-cert/no-external-db localhost execution:
docker run -it --rm -p 80:8080 -p 443:8443 -h localhost -e TLS_SETUP_ENABLED="simple" qrypt/ejbca-ce:1.0-SNAPSHOT


## Older info: 

Update Java Security File for when you execute a command via the terminal with "java com.qrypt.randomprovider.Main"

* `sudo vim /docker-java-home/conf/security/java.security`
* Add this line: `security.provider.1=com.qrypt.QRNGRandomProvider`
* Bump the rest of the providers down in priority


Update the Java Security File for when you execute a command via VS Code
* `sudo vim /home/vscode/.vscode-server/extensions/redhat.java-1.33.0-linux-arm64/jre/17.0.11-linux-aarch64/conf/security/java.security`
* Add this line: `security.provider.1=com.qrypt.QRNGRandomProvider`
* Bump the rest of the providers down in priority



# Run
From the random-provider folder:
cd target/classes;
java com.qrypt.randomprovider.Main;




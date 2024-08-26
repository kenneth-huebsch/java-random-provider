# Helpful links
(GetRandom Provide Example - Github)[https://github.com/marschall/getrandom-provider]

# Update Java Security File for when you execute a command via the terminal with "java com.qrypt.Main"
`sudo vim /docker-java-home/conf/security/java.security`
Add this line: `security.provider.1=com.qrypt.QRNGRandomProvider`
Bump the rest of the providers down in priority

# Update the Java Security File for when you execute a command via VS Code
`sudo vim /home/vscode/.vscode-server/extensions/redhat.java-1.33.0-linux-arm64/jre/17.0.11-linux-aarch64/conf/security/java.security`
Add this line: `security.provider.1=com.qrypt.QRNGRandomProvider`
Bump the rest of the providers down in priority

# Build
From the random-provider folder:
mvn compile;
rm target/classes/com/qrypt/*.jar;
jar cf target/classes/com/qrypt/QRNGRandomProvider.jar -C /workspaces/java-random-provider/random-provider/target/classes/com/qrypt QRNGRandomProvider.class;

# Install jar file for when you execute a command via the terminal with "java com.qrypt.Main"
Copy the jar file to "/docker-java-home/lib"

# Install the jar file for when you execute a command via VS Code
Copy the jar file to this folder: "/home/vscode/.vscode-server/extensions/redhat.java-1.33.0-linux-arm64/jre/17.0.11-linux-aarch64/lib"



# Run
From the random-provider folder:
cd target/classes;
java com.qrypt.Main;

## Hash code in case its useful
    private byte[] SHA512Hash(byte[] bytes){
        final int SHA512_RETURN_LENGTH = 64;

        // List to store all hashed chunks
        byte[] returnValue = new byte[bytes.length];

        try {
            // Get SHA-512 MessageDigest instance
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

            // Process the input in chunks of 64 bytes
            for (int i = 0; i < bytes.length; i += SHA512_RETURN_LENGTH) {
                // Calculate the chunk size (handle last chunk if smaller than 64 bytes)
                int chunkSize = Math.min(SHA512_RETURN_LENGTH, bytes.length - i);
                
                // Copy the chunk
                byte[] chunk = new byte[chunkSize];
                System.arraycopy(bytes, i, chunk, 0, chunkSize);
                
                // Hash the chunk and add the result to the return value
                byte[] hashedChunk = sha512.digest(chunk);
                System.arraycopy(hashedChunk, 0, returnValue, i, chunkSize);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        }

        return returnValue;
    }



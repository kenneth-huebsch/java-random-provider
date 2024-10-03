package com.qrypt.randomprovider.demo2;


import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/securerandom")
public class SecureRandomServlet2 extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(SecureRandomServlet2.class);
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html;charset=UTF-8");


        // Initialize SecureRandom
        SecureRandom secureRandom = null;
        try {
            secureRandom = new SecureRandom();//SecureRandom.getInstance("QRNGRestAPI"); // Uses a strong algorithm
        } catch (Exception e) {
            logger.error("Error initializing SecureRandom with Qrypt, defaulting to others", e);
            // Fallback to default SecureRandom instance
            throw new RuntimeException(e.getMessage());
        }

        SecureRandom secureRandomStrong = null;
        try {
            secureRandomStrong = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error initializing SecureRandom with Strong", e);
            throw new RuntimeException(e.getMessage());
        }

        // Generate random bytes
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);

        // Generate seed
        byte[] seed = secureRandom.generateSeed(16);

        // Get provider information
        String providerName = secureRandom.getProvider().getName();
        String algorithmName = secureRandom.getAlgorithm();

        // Convert byte arrays to hex strings
        String randomBytesHex = bytesToHex(randomBytes);
        String seedHex = bytesToHex(seed);

        // Generate random bytes
        byte[] randomBytes2 = new byte[16];
        secureRandomStrong.nextBytes(randomBytes2);

        // Generate seed
        byte[] seed2 = secureRandomStrong.generateSeed(16);

        // Get provider information
        String providerName2 = secureRandomStrong.getProvider().getName();
        String algorithmName2 = secureRandomStrong.getAlgorithm();

        // Convert byte arrays to hex strings
        String randomBytesHex2 = bytesToHex(randomBytes2);
        String seedHex2 = bytesToHex(seed2);

        // Output the results
        try (PrintWriter out = response.getWriter()) {
            out.println("<html>");
            out.println("<head><title>SecureRandom Demo</title></head>");
            out.println("<body>");
            out.println("<h1>SecureRandom Demo</h1>");
            out.println("<p><strong>Provider Name:</strong> " + providerName + "</p>");
            out.println("<p><strong>Algorithm:</strong> " + algorithmName + "</p>");
            out.println("<p><strong>Random Bytes (Hex):</strong> " + randomBytesHex + "</p>");
            out.println("<p><strong>Seed Bytes (Hex):</strong> " + seedHex + "</p>");
            out.println("<p><strong>Strong Algorithm Provider Name:</strong> " + providerName2 + "</p>");
            out.println("<p><strong>Strong Algorithm:</strong> " + algorithmName2 + "</p>");
            out.println("<p><strong>Strong Algorithm Random Bytes (Hex):</strong> " + randomBytesHex2 + "</p>");
            out.println("<p><strong>Strong Algorithm Seed Bytes (Hex):</strong> " + seedHex2 + "</p>");

            out.println("</body>");
            out.println("</html>");
        }
    }

    // Helper method to convert byte array to hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}


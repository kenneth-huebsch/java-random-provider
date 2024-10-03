package com.qrypt.randomprovider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AppPropertiesTests {

    @BeforeAll
    public static void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = AppPropertiesTests.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (input == null) {
                throw new IOException("Unable to find app.properties in test/resources");
            }
            properties.load(input);

            // Set each property as a system property
            for (String propertyName : properties.stringPropertyNames()) {
                System.setProperty(propertyName, properties.getProperty(propertyName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProperty1() {
        String property1 = System.getProperty("qrypt.api.url");
        assertEquals("https://qrypt.com/api", property1, "qrypt.api.url should be 'value1'");
    }

    @Test
    public void testProperty2() {
        String property2 = System.getProperty("qrypt.token");
        assertEquals("1234", property2, "qrypt.token should be 'value2'");
    }

}

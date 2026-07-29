package collectors.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/secret.properties")) {
            if (in == null) throw new IllegalStateException("secret.properties not found in resources");
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load secret.properties", e);
        }
    }

    public static String get(String key) {
        String v = props.getProperty(key);
        if (v == null || v.isBlank()) throw new IllegalStateException("Missing config: " + key);
        return v.trim();
    }
}
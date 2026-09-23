package org.forestwizard.goitjavacoreexamproject.util;

import java.io.IOException;
import java.io.InputStream;

public class ResourceLoader {
    private ResourceLoader() {}

    public static String loadResource(String path) throws ResourceLoaderException {
        try (InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new ResourceLoaderException("The input stream is null");
            }
            return new String(stream.readAllBytes());
        } catch (IOException e) {
            throw new ResourceLoaderException("I/O Error: " + e.getMessage(), e);
        }
    }
}

package uk.gov.pay.stubs;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

public class TestUtils {

    public static String readFile(String filename) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
        File file = new File(url.getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}

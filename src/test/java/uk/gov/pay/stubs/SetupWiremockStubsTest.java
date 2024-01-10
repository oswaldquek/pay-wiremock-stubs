package uk.gov.pay.stubs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetupWiremockStubsTest {

    private HttpRequest.Builder request;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void setup() throws Exception {
        request = HttpRequest.newBuilder().uri(new URI("https://lrmjj.wiremockapi.cloud/stub/worldpay"));
    }

    @Test
    void shouldReturnUnauthorizedIfNoAuthHeaderProvided() throws Exception {
        HttpRequest httpRequest = request.POST(BodyPublishers.ofString(readFile("authRequest.xml")))
                .header("Content-Type", "application/xml")
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, httpResponse.statusCode());
    }

    @Test
    void shouldReturnAnAuthorisation() throws Exception {
        HttpRequest httpRequest = request.POST(BodyPublishers.ofString(readFile("authRequest.xml")))
                .header("Content-Type", "application/xml")
                .header("Authorization", "a-secret")
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, httpResponse.statusCode());
        assertEquals("text/xml;charset=utf-8", httpResponse.headers().firstValue("Content-Type").get());
        assertEquals(authSuccessResponse(), httpResponse.body());
    }

    private String authSuccessResponse() throws Exception {
        return readFile("authSuccessResponse.xml");
    }

    @Test
    void shouldSucceedOnFirstAuth3DSRequest() throws Exception {
        HttpRequest httpRequest = request.POST(BodyPublishers.ofString(readFile("authFirst3DSRequest.xml")))
                .header("Content-Type", "application/xml")
                .header("Authorization", "a-secret")
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, httpResponse.statusCode());
        assertEquals("text/xml;charset=utf-8", httpResponse.headers().firstValue("Content-Type").get());
        assertEquals(authFirst3DSSuccessResponse(), httpResponse.body());
    }

    private String authFirst3DSSuccessResponse() throws Exception {
        return readFile("authFirst3DSSuccessResponse.xml");
    }

    @Test
    void shouldSucceedOnSecondAuth3DSRequest() throws Exception {
        HttpRequest httpRequest = request.POST(BodyPublishers.ofString(readFile("authSecond3DSRequest.xml")))
                .header("Content-Type", "application/xml")
                .header("Authorization", "a-secret")
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, httpResponse.statusCode());
        assertEquals("text/xml;charset=utf-8", httpResponse.headers().firstValue("Content-Type").get());
        assertEquals(authSuccessResponse(), httpResponse.body());
    }

    private String readFile(String filename) throws Exception {
        URL url = getClass().getClassLoader().getResource(filename);
        File file = new File(url.getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
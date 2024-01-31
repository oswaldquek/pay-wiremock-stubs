package uk.gov.pay.stubs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.pay.stubs.TestUtils.readFile;

class WorldpayStubsTest {

    private HttpRequest.Builder worldpayStub;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void setup() throws Exception {
        worldpayStub = HttpRequest.newBuilder().uri(new URI("https://lrmjj.wiremockapi.cloud/stub/worldpay"));
    }

    @Test
    void shouldReturnUnauthorizedIfNoAuthHeaderProvided() throws Exception {
        HttpRequest httpRequest = worldpayStub.POST(BodyPublishers.ofString(readFile("authRequest.xml")))
                .header("Content-Type", "application/xml")
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, httpResponse.statusCode());
    }

    @Test
    void shouldReturnAnAuthorisation() throws Exception {
        HttpRequest httpRequest = worldpayStub.POST(BodyPublishers.ofString(readFile("authRequest.xml")))
                .header("Content-Type", "application/xml")
                .header("Authorization", "a-secret")
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, httpResponse.statusCode());
        assertEquals("text/xml;charset=utf-8", httpResponse.headers().firstValue("Content-Type").get());
        assertEquals(readFile("expectedAuthSuccessResponse.xml"), httpResponse.body());
    }

    @Test
    void shouldSucceedOnFirstAuth3DSRequest() throws Exception {
        HttpRequest httpRequest = worldpayStub.POST(BodyPublishers.ofString(readFile("authFirst3DSRequest.xml")))
                .header("Content-Type", "application/xml")
                .header("Authorization", "a-secret")
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, httpResponse.statusCode());
        assertEquals("text/xml;charset=utf-8", httpResponse.headers().firstValue("Content-Type").get());
        assertEquals(readFile("expectedAuthFirst3DSSuccessResponse.xml"), httpResponse.body());
    }

    @Test
    void shouldSucceedOnSecondAuth3DSRequest() throws Exception {
        HttpRequest httpRequest = worldpayStub.POST(BodyPublishers.ofString(readFile("authSecond3DSRequest.xml")))
                .header("Content-Type", "application/xml")
                .header("Authorization", "a-secret")
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, httpResponse.statusCode());
        assertEquals("text/xml;charset=utf-8", httpResponse.headers().firstValue("Content-Type").get());
        assertEquals(readFile("expectedAuthSuccessResponse.xml"), httpResponse.body());
    }

    @Test
    void shouldReturnACapture() throws Exception {
        HttpRequest httpRequest = worldpayStub.POST(BodyPublishers.ofString(readFile("captureRequest.xml")))
                .header("Content-Type", "application/xml")
                .header("Authorization", "a-secret")
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, httpResponse.statusCode());
        assertEquals("text/xml;charset=utf-8", httpResponse.headers().firstValue("Content-Type").get());
        assertEquals(readFile("captureSuccessResponse.xml"), httpResponse.body());
    }
}
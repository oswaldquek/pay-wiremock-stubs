package uk.gov.pay.stubs;

import com.google.common.net.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class SetupWiremockStubsTest {

    private HttpRequest.Builder request;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void setup() throws Exception {
        request = HttpRequest.newBuilder().uri(new URI("https://lrmjj.wiremockapi.cloud/stub/worldpay"));
    }

    @Test
    void shouldReturnUnauthorizedIfNoAuthHeaderProvided() throws Exception {
        HttpRequest httpRequest = request.POST(HttpRequest.BodyPublishers.ofInputStream(() -> getClass().getClassLoader().getResourceAsStream("authRequest.xml")))
                .header("Content-Type", "application/xml")
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, httpResponse.statusCode());
    }
}
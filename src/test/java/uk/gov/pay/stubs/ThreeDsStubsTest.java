package uk.gov.pay.stubs;

import net.minidev.json.JSONObject;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.pay.stubs.TestUtils.readFile;

public class ThreeDsStubsTest {

    private HttpRequest.Builder threeDsStub;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void setup() throws Exception {
        URIBuilder builder = new URIBuilder("https://lrmjj.wiremockapi.cloud/3ds-simulator");
        builder.setParameter("orderCode", "abcde-fgh");
        threeDsStub = HttpRequest.newBuilder().uri(builder.build());
    }

    @ParameterizedTest
    @MethodSource("threeDsArguments")
    void shouldReturn3DSiFrameHtml(String paReq, String expectedHtml) throws Exception {
        var data = Map.of("MD", "some-md", "TermUrl", "/card_details/external-charge-id/3ds_required_in", "PaReq", paReq);
        HttpRequest httpRequest = threeDsStub.POST(HttpRequest.BodyPublishers.ofString(new JSONObject(data).toJSONString())).build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, httpResponse.statusCode());
        assertEquals("text/html", httpResponse.headers().firstValue("Content-Type").get());
        String expected = readFile(expectedHtml).replaceAll("\n", "").replace(" ", "");
        String actual = httpResponse.body().replaceAll("\n", "").replace(" ", "");
        assertEquals(expected, actual);
    }

    static Stream<Arguments> threeDsArguments() {
        return Stream.of(
                Arguments.of("aPaReq", "expected3DSiFrame.html"),
                Arguments.of("authorisation_fail", "expectedAuthorisationFail3DSiFrame.html")
        );
    }
}

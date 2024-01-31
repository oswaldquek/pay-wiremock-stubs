package uk.gov.pay.stubs;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.security.ClientTokenAuthenticator;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static org.wiremock.webhooks.Webhooks.webhook;

public class SetupWiremockStubs {

    private final static WireMock wm;

    static {
        wm = WireMock.create()
                .scheme("https")
                .host("lrmjj.wiremockapi.cloud")
                .port(443)
                .authenticator(new ClientTokenAuthenticator(System.getenv("WIREMOCK_API_TOKEN")))
                .build();
    }

    public static void main(String[] args) throws Exception {
//        wm.removeStubMapping(); TODO Figure out how to remove all mappings to prevent duplicates on wiremock cloud
        var setupWiremockStubs = new SetupWiremockStubs();
        setupWiremockStubs.stubUnauthorized();
        setupWiremockStubs.stubReturnAuthorisation();
        setupWiremockStubs.stubFirst3DSSuccessResponse();
        setupWiremockStubs.stubSecond3DSSuccessResponse();
        setupWiremockStubs.stubCaptureSuccessResponse();
        setupWiremockStubs.stubHtml3dsSimulator();
    }

    private void stubHtml3dsSimulator() throws Exception {
//        wm.register(post(urlEqualTo("/3ds-simulator"))
        wm.register(post(urlMatching("/3ds-simulator\\?orderCode=.*"))
//                .withQueryParam("orderCode", not(absent())) // This is ideal but doesn't work :(
                .willReturn(aResponse()
                        .withStatus(200)
                        .withTransformers("response-template") // This is required to <a href="https://docs.wiremock.io/response-templating/basics/">Enable dynamic response templating</a>
                        .withBody(readFile("3dsSimulator.html"))
                        .withHeader("Content-Type", "text/html")));
    }

    private void stubUnauthorized() {
        wm.register(post(urlEqualTo("/stub/worldpay")).atPriority(10) //1 is highest
                .willReturn(aResponse().withStatus(401)));
    }

    private void stubReturnAuthorisation() throws Exception {
        wm.register(post(urlEqualTo("/stub/worldpay")).atPriority(4)
                .withHeader("Authorization", matching(".*"))
                .withHeader("Content-Type", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml;charset=utf-8")
                        .withTransformers("response-template")
                        .withBody(readFile("authSuccessResponse.xml")))
        );
    }

    private void stubFirst3DSSuccessResponse() throws Exception {
        wm.register(post(urlEqualTo("/stub/worldpay")).atPriority(1)
                .withHeader("Authorization", matching(".*"))
                .withHeader("Content-Type", equalTo("application/xml"))
                .withRequestBody(matchingXPath("/paymentService/submit/order/shopper"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml;charset=utf-8")
                        .withTransformers("response-template")
                        .withBody(readFile("authFirst3DSSuccessResponse.xml")))
        );
    }

    private void stubSecond3DSSuccessResponse() throws Exception {
        wm.register(post(urlEqualTo("/stub/worldpay")).atPriority(2)
                .withHeader("Authorization", matching(".*"))
                .withHeader("Content-Type", equalTo("application/xml"))
                .withRequestBody(matchingXPath("/paymentService/submit/order/info3DSecure"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml;charset=utf-8")
                        .withTransformers("response-template")
                        .withBody(readFile("authSuccessResponse.xml")))
        );
    }

    private void stubCaptureSuccessResponse() throws Exception {
        wm.register(post(urlEqualTo("/stub/worldpay")).atPriority(3)
                .withHeader("Authorization", matching(".*"))
                .withHeader("Content-Type", equalTo("application/xml"))
                .withRequestBody(matchingXPath("/paymentService/modify/orderModification/capture"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml;charset=utf-8")
                        .withBody(readFile("captureSuccessResponse.xml")))
                        .withServeEventListener("webhook", webhook()
                                .withFixedDelay(5000)
                                .withMethod(POST)
                                .withUrl(System.getenv("WEBHOOK_URL"))
                                .withHeader("Content-Type", "text/xml")
                                .withBody(readFile("notification.xml")))
        );
    }

    private String readFile(String filename) throws Exception {
        URL url = getClass().getClassLoader().getResource(filename);
        File file = new File(url.getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}

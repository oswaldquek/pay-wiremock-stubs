package uk.gov.pay.stubs;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.security.ClientTokenAuthenticator;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class SetupWiremockStubs {

    private final static WireMock wm;

    static {
        wm = WireMock.create()
                .scheme("https")
                .host("lrmjj.wiremockapi.cloud")
                .port(443)
                .authenticator(new ClientTokenAuthenticator(System.getenv("WIREMOCK_API_TOKEN")))
                .build();
        WireMock.configureFor(wm);
    }

    public static void main(String[] args) throws Exception {
//        wm.removeStubMapping(); TODO Figure out how to remove all mappings to prevent duplicates on wiremock cloud
        var setupWiremockStubs = new SetupWiremockStubs();
        setupWiremockStubs.stubUnauthorized();
        setupWiremockStubs.stubReturnAuthorisation();
    }

    private void stubUnauthorized() {
        wm.register(post(urlEqualTo("/stub/worldpay")).atPriority(10) //1 is highest
                .willReturn(aResponse()
                        .withStatus(401)));
    }

    private void stubReturnAuthorisation() throws Exception {
        wm.register(post(urlEqualTo("/stub/worldpay")).atPriority(1)
                        .withHeader("Authorization", matching(".*"))
                        .withHeader("Content-Type", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml;charset=utf-8")
                        .withBody(readFile("authSuccessResponse.xml"))));
    }

    private String readFile(String filename) throws Exception {
        URL url = getClass().getClassLoader().getResource(filename);
        File file = new File(url.getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}

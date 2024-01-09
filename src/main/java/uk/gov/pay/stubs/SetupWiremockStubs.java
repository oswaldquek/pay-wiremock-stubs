package uk.gov.pay.stubs;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.security.ClientTokenAuthenticator;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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

    public static void main(String[] args) {
//        wm.removeStubMapping(); TODO Figure out how to remove all mappings to prevent duplicates on wiremock cloud
        stubUnauthorized();
    }

    private static void stubUnauthorized() {
        wm.register(post(urlEqualTo("/stub/worldpay")).atPriority(10) //1 is highest
                .willReturn(aResponse()
                        .withStatus(401)));
    }
}

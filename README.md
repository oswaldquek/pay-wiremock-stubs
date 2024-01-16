# pay-wiremock-stubs

Intended to replace GOV.UK Pay's pay-stubs (private) project. The motivations for doing so are as follows:

* Pay's skill set is more geared towards Java, but pay-stubs is built in Node.
* Related to the above, pay-stubs is a running server we have to maintain and uses 38 libraries at the time
of writing. In contrast this pay-wiremock-stubs project only requires 3 libraries.
* We've been wanting to [make pay-stubs send capture notifications](https://payments-platform.atlassian.net/browse/PP-10886)
since 3/4/23. This would ensure we're accurately performance testing our environment.
* Java devs already use Wiremock for integration tests so there is familiarity with the DSL.
* Stubbing becomes a matter of configuring the Wiremock SaaS service, rather than coding a stub and making 
it go through a deployment pipeline.

## How to run

I have a Wiremock cloud account at https://lrmjj.wiremockapi.cloud. To set up the stubs, run the `main` method
of SetupWiremockStubs. This requires a Wiremock token which corresponds to an environment variable 
WIREMOCK_API_TOKEN. Note that if you re-run `main` you'll set up duplicate stubs in the Wiremock account.
There is a TODO on how to make sure this cannot happen.

Run SetupWiremockStubsTest to verify the stubs have been set up correctly on Wiremock cloud. These tests
should correspond to the unit tests in pay-stubs.

## How to demo

* Spin up ngrok server and get the public address.
* Set the WEBHOOK_URL env var to the public address got from above.
* Clear out the stubs on the [stubs page](https://app.wiremock.cloud/mock-apis/lrmjj/stubs/c28f6ff7-d043-429a-9dee-cfe53c738d91).
* Run `SetupWiremockStubs.main` and verify stubs have been created.
* Run `SetupWiremockStubsTest` and verify all tests pass.
* Navigate to http://localhost:4040/inspect/http (this is an ngrok feature) to verify the webhook for the capture request was received.
Verify the orderCode matches the orderCode in the captureRequest.xml.

/*
Copyright 2020 The Kubernetes Authors.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package io.kubernetes.client.util.version;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.ClientBuilder;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class VersionTest {

  private ApiClient client;

  @Rule public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  @Before
  public void setup() {
    client = new ClientBuilder().setBasePath("http://localhost:" + wireMockRule.port()).build();
  }

  @Test
  public void testUrl() throws IOException, ApiException {

    wireMockRule.stubFor(
        get(urlPathEqualTo("/version/"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody("{\"gitVersion\":\"\",\"gitCommit\":\"\",\"major\":\"\",\"minor\":\"\"," +
                            "\"goVersion\":\"\",\"buildDate\":\"\",\"compiler\":\"\"," +
                            "\"gitTreeState\":\"\",\"platform\":\"\"}")));

    Version versionUtil = new Version(client);
    assertThat(versionUtil.getVersion()).isNotNull();

    verify(
        getRequestedFor(urlPathEqualTo("/version/"))
            .withHeader("Accept", equalTo("application/json")));
  }

  @Test
  public void testFailure() throws IOException, ApiException {

    wireMockRule.stubFor(
        get(urlPathEqualTo("/version/"))
            .willReturn(
                aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));

    Version versionUtil = new Version(client);
    try {
      versionUtil.getVersion();
      failBecauseExceptionWasNotThrown(ApiException.class);
    } catch (ApiException ex) {
      assertThat(ex.getCode()).isEqualTo(401);
    }

    verify(
        getRequestedFor(urlPathEqualTo("/version/"))
            .withHeader("Accept", equalTo("application/json")));
  }
}

/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.filter;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.test.BaseWebClientTests;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.gateway.httpclient.response-timeout=3s", webEnvironment = RANDOM_PORT)
@DirtiesContext
@SuppressWarnings("unchecked")
public class NettyRoutingFilterIntegrationTests extends BaseWebClientTests {

	@Test
	public void responseTimeoutWorks() {
		testClient.get().uri("/delay/5").exchange().expectStatus()
				.isEqualTo(HttpStatus.GATEWAY_TIMEOUT).expectBody().jsonPath("$.status")
				.isEqualTo(String.valueOf(HttpStatus.GATEWAY_TIMEOUT.value()))
				.jsonPath("$.message")
				.isEqualTo("Response took longer than timeout: PT3S");
	}

	@Test
	public void outboundHostHeaderNotOverwrittenByInbound() {
		// different base url to have different host header in inbound / outbound requests
		// Host: 127.0.0.1 -> request to Gateway, Host: localhost -> request from Gateway,
		// resolved from lb://testservice
		WebTestClient client = testClient.mutate().baseUrl("http://127.0.0.1:" + port)
				.build();

		client.get().uri("/headers").exchange().expectBody().jsonPath("$.headers.host")
				.isEqualTo("localhost:" + port);
	}

	@EnableAutoConfiguration
	@SpringBootConfiguration
	@Import(DefaultTestConfig.class)
	public static class TestConfig {

	}

}

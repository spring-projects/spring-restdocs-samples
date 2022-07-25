/*
 * Copyright 2014-2022 the original author or authors.
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

package com.example.restassured;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class SampleRestAssuredApplicationTests {

	private RequestSpecification documentationSpec;

	@LocalServerPort
	private int port;

	@BeforeEach
	public void setUp(RestDocumentationContextProvider restDocumentation) {
		this.documentationSpec = new RequestSpecBuilder()
				.addFilter(documentationConfiguration(restDocumentation)).build();
	}

	@Test
	public void sample() {
		given(this.documentationSpec)
				.accept("text/plain")
				.filter(document("sample",
						preprocessRequest(modifyUris()
								.scheme("https")
								.host("api.example.com")
								.removePort())))
		.when()
				.port(this.port)
				.get("/")
		.then()
				.assertThat().statusCode(is(200));
	}

}

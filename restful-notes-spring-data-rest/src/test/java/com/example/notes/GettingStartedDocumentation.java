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

package com.example.notes;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
class GettingStartedDocumentation {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(restDocumentation))
				.alwaysDo(document("{method-name}/{step}/"))
				.build();
	}

	@Test
	void index() throws Exception {
		this.mockMvc.perform(get("/").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("_links.notes", is(notNullValue())))
			.andExpect(jsonPath("_links.tags", is(notNullValue())));
	}

	@Test
	void creatingANote() throws JsonProcessingException, Exception {
		String noteLocation = createNote();
		MvcResult note = getNote(noteLocation);

		String tagLocation = createTag();
		getTag(tagLocation);

		String taggedNoteLocation = createTaggedNote(tagLocation);
		MvcResult taggedNote = getNote(taggedNoteLocation);
		getTags(getLink(taggedNote, "tags"));

		tagExistingNote(noteLocation, tagLocation);
		getTags(getLink(note, "tags"));
	}

	private String createNote() throws Exception {
		Map<String, String> note = new HashMap<String, String>();
		note.put("title", "Note creation with cURL");
		note.put("body", "An example of how to create a note using cURL");

		String noteLocation = this.mockMvc
				.perform(
						post("/notes").contentType(MediaTypes.HAL_JSON).content(
								objectMapper.writeValueAsString(note)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", notNullValue()))
				.andReturn().getResponse().getHeader("Location");
		return noteLocation;
	}

	private MvcResult getNote(String noteLocation) throws Exception {
		return this.mockMvc.perform(get(noteLocation))
				.andExpect(status().isOk())
				.andExpect(jsonPath("title", is(notNullValue())))
				.andExpect(jsonPath("body", is(notNullValue())))
				.andExpect(jsonPath("_links.tags", is(notNullValue())))
				.andReturn();
	}

	private String createTag() throws Exception, JsonProcessingException {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "getting-started");

		String tagLocation = this.mockMvc
				.perform(
						post("/tags").contentType(MediaTypes.HAL_JSON).content(
								objectMapper.writeValueAsString(tag)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", notNullValue()))
				.andReturn().getResponse().getHeader("Location");
		return tagLocation;
	}

	private void getTag(String tagLocation) throws Exception {
		this.mockMvc.perform(get(tagLocation)).andExpect(status().isOk())
			.andExpect(jsonPath("name", is(notNullValue())))
			.andExpect(jsonPath("_links.notes", is(notNullValue())));
	}

	private String createTaggedNote(String tag) throws Exception {
		Map<String, Object> note = new HashMap<String, Object>();
		note.put("title", "Tagged note creation with cURL");
		note.put("body", "An example of how to create a tagged note using cURL");
		note.put("tags", Arrays.asList(tag));

		String noteLocation = this.mockMvc
				.perform(
						post("/notes").contentType(MediaTypes.HAL_JSON).content(
								objectMapper.writeValueAsString(note)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", notNullValue()))
				.andReturn().getResponse().getHeader("Location");
		return noteLocation;
	}

	private void getTags(String noteTagsLocation) throws Exception {
		this.mockMvc.perform(get(noteTagsLocation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("_embedded.tags", hasSize(1)));
	}

	private void tagExistingNote(String noteLocation, String tagLocation) throws Exception {
		Map<String, Object> update = new HashMap<String, Object>();
		update.put("tags", Arrays.asList(tagLocation));

		this.mockMvc.perform(
				patch(noteLocation).contentType(MediaTypes.HAL_JSON).content(
						objectMapper.writeValueAsString(update)))
				.andExpect(status().isNoContent());
	}

	private String getLink(MvcResult result, String rel)
			throws UnsupportedEncodingException {
		return JsonPath.parse(result.getResponse().getContentAsString()).read(
				"_links." + rel + ".href");
	}

}
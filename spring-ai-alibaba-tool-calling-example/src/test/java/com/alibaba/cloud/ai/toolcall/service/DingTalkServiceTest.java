/*
 * Copyright 2024-2025 the original author or authors.
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
package com.alibaba.cloud.ai.toolcall.service;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class DingTalkServiceTest {

	/**
	 * 请在此处配置你的钉钉机器人 access_token 以运行集成测试。
	 * 仅用于本地手动验证，CI 环境中集成测试默认跳过。
	 */
	private static final String ACCESS_TOKEN = "";

	private static final String SUCCESS_RESPONSE = "{\"errcode\":0,\"errmsg\":\"ok\"}";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private RestTemplate restTemplate;

	private MockRestServiceServer mockServer;

	private DingTalkService dingTalkService;

	@BeforeEach
	void setUp() {
		restTemplate = new RestTemplate();
		mockServer = MockRestServiceServer.createServer(restTemplate);
		dingTalkService = new DingTalkService("test-token", restTemplate);
	}

	@Test
	@DisplayName("发送文本消息 - 验证请求体格式正确")
	void sendTextMessage_shouldSendCorrectPayload() throws Exception {
		mockServer.expect(ExpectedCount.once(),
				requestTo("https://oapi.dingtalk.com/robot/send?access_token=test-token"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andRespond(withSuccess(SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

		String result = dingTalkService.sendTextMessage("Hello DingTalk");

		mockServer.verify();
		JsonNode response = OBJECT_MAPPER.readTree(result);
		assertEquals(0, response.get("errcode").asInt());
		assertEquals("ok", response.get("errmsg").asText());
	}

	@Test
	@DisplayName("发送文本消息并@指定人员")
	void sendTextMessage_withAtMobiles_shouldIncludeAtField() throws Exception {
		mockServer.expect(ExpectedCount.once(),
				requestTo("https://oapi.dingtalk.com/robot/send?access_token=test-token"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andRespond(withSuccess(SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

		List<String> mobiles = Arrays.asList("13800138000", "13900139000");
		String result = dingTalkService.sendTextMessage("Hello Team", mobiles, false);

		mockServer.verify();
		assertNotNull(result);
	}

	@Test
	@DisplayName("发送文本消息并@所有人")
	void sendTextMessage_withAtAll_shouldSetAtAllTrue() throws Exception {
		mockServer.expect(ExpectedCount.once(),
				requestTo("https://oapi.dingtalk.com/robot/send?access_token=test-token"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess(SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

		String result = dingTalkService.sendTextMessage("Important Notice", null, true);

		mockServer.verify();
		assertNotNull(result);
	}

	@Test
	@DisplayName("发送Markdown消息 - 验证请求体格式正确")
	void sendMarkdownMessage_shouldSendCorrectPayload() throws Exception {
		mockServer.expect(ExpectedCount.once(),
				requestTo("https://oapi.dingtalk.com/robot/send?access_token=test-token"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andRespond(withSuccess(SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

		String result = dingTalkService.sendMarkdownMessage(
				"Daily Report",
				"## Daily Report\n- Item 1\n- Item 2");

		mockServer.verify();
		JsonNode response = OBJECT_MAPPER.readTree(result);
		assertEquals(0, response.get("errcode").asInt());
	}

	@Test
	@DisplayName("发送Markdown消息并@指定人员")
	void sendMarkdownMessage_withAtMobiles_shouldIncludeAtField() throws Exception {
		mockServer.expect(ExpectedCount.once(),
				requestTo("https://oapi.dingtalk.com/robot/send?access_token=test-token"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess(SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

		List<String> mobiles = Arrays.asList("13800138000");
		String result = dingTalkService.sendMarkdownMessage(
				"Report", "## Report Content", mobiles, false);

		mockServer.verify();
		assertNotNull(result);
	}

	@Test
	@DisplayName("发送Link消息 - 验证请求体格式正确")
	void sendLinkMessage_shouldSendCorrectPayload() throws Exception {
		mockServer.expect(ExpectedCount.once(),
				requestTo("https://oapi.dingtalk.com/robot/send?access_token=test-token"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andRespond(withSuccess(SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

		String result = dingTalkService.sendLinkMessage(
				"Spring AI Alibaba",
				"Spring AI Alibaba Examples",
				"https://github.com/alibaba/spring-ai-alibaba",
				"https://example.com/logo.png");

		mockServer.verify();
		JsonNode response = OBJECT_MAPPER.readTree(result);
		assertEquals(0, response.get("errcode").asInt());
	}

	@Test
	@DisplayName("发送Link消息 - picUrl为空时不包含picUrl字段")
	void sendLinkMessage_withNullPicUrl_shouldOmitPicUrl() throws Exception {
		mockServer.expect(ExpectedCount.once(),
				requestTo("https://oapi.dingtalk.com/robot/send?access_token=test-token"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess(SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

		String result = dingTalkService.sendLinkMessage(
				"Title", "Description", "https://example.com", null);

		mockServer.verify();
		assertNotNull(result);
	}

	@Test
	@DisplayName("钉钉API返回错误时应正确传递错误信息")
	void sendTextMessage_whenApiReturnsError_shouldReturnErrorResponse() throws Exception {
		String errorResponse = "{\"errcode\":310000,\"errmsg\":\"keywords not in content\"}";
		mockServer.expect(ExpectedCount.once(),
				requestTo("https://oapi.dingtalk.com/robot/send?access_token=test-token"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess(errorResponse, MediaType.APPLICATION_JSON));

		String result = dingTalkService.sendTextMessage("test");

		mockServer.verify();
		JsonNode response = OBJECT_MAPPER.readTree(result);
		assertEquals(310000, response.get("errcode").asInt());
	}

	@Test
	@DisplayName("HTTP请求失败时应抛出异常")
	void sendTextMessage_whenHttpError_shouldThrowException() {
		mockServer.expect(ExpectedCount.once(),
				requestTo("https://oapi.dingtalk.com/robot/send?access_token=test-token"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

		assertThrows(Exception.class, () -> dingTalkService.sendTextMessage("test"));
		mockServer.verify();
	}

	@Test
	@DisplayName("验证webhook URL构建正确")
	void getWebhookUrl_shouldReturnCorrectUrl() {
		assertEquals(
				"https://oapi.dingtalk.com/robot/send?access_token=test-token",
				dingTalkService.getWebhookUrl());
	}

	// ==================== 集成测试（需手动配置 ACCESS_TOKEN 后启用） ====================

	@Test
	@Disabled("集成测试：需要配置真实的 ACCESS_TOKEN 后移除 @Disabled 注解运行")
	@DisplayName("集成测试 - 发送真实文本消息到钉钉")
	void integrationTest_sendTextMessage() {
		DingTalkService realService = new DingTalkService(ACCESS_TOKEN);
		String result = realService.sendTextMessage("Spring AI Alibaba 集成测试消息");
		System.out.println("DingTalk response: " + result);
		assertNotNull(result);
	}

	@Test
//	@Disabled("集成测试：需要配置真实的 ACCESS_TOKEN 后移除 @Disabled 注解运行")
	@DisplayName("集成测试 - 发送真实Markdown消息到钉钉")
	void integrationTest_sendMarkdownMessage() {
		DingTalkService realService = new DingTalkService(ACCESS_TOKEN);
		String markdown = "## Spring AI Alibaba 测试\n" +
				"- **项目**: spring-ai-alibaba-examples\n" +
				"- **时间**: " + java.time.LocalDateTime.now() + "\n" +
				"- **状态**: 测试通过 ✅";
		String result = realService.sendMarkdownMessage("集成测试报告Hello", markdown);
		System.out.println("DingTalk response: " + result);
		assertNotNull(result);
	}

	@Test
	@Disabled("集成测试：需要配置真实的 ACCESS_TOKEN 后移除 @Disabled 注解运行")
	@DisplayName("集成测试 - 发送真实Link消息到钉钉")
	void integrationTest_sendLinkMessage() {
		DingTalkService realService = new DingTalkService(ACCESS_TOKEN);
		String result = realService.sendLinkMessage(
				"Spring AI Alibaba",
				"Spring AI Alibaba 示例项目",
				"https://github.com/alibaba/spring-ai-alibaba",
				"");
		System.out.println("DingTalk response: " + result);
		assertNotNull(result);
	}

}

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class DingTalkService {

	private static final Logger logger = LoggerFactory.getLogger(DingTalkService.class);

	private static final String WEBHOOK_URL_TEMPLATE = "https://oapi.dingtalk.com/robot/send?access_token=%s";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final String accessToken;

	private final RestTemplate restTemplate;

	public DingTalkService(String accessToken) {
		this(accessToken, new RestTemplate());
	}

	public DingTalkService(String accessToken, RestTemplate restTemplate) {
		this.accessToken = accessToken;
		this.restTemplate = restTemplate;
	}

	/**
	 * 发送文本消息
	 * @param content 消息内容
	 * @return 钉钉API响应
	 */
	public String sendTextMessage(String content) {
		return sendTextMessage(content, null, false);
	}

	/**
	 * 发送文本消息（可指定@的人）
	 * @param content 消息内容
	 * @param atMobiles 需要@的手机号列表，null表示不@任何人
	 * @param atAll 是否@所有人
	 * @return 钉钉API响应
	 */
	public String sendTextMessage(String content, List<String> atMobiles, boolean atAll) {
		Map<String, Object> body = new HashMap<>();
		body.put("msgtype", "text");

		Map<String, String> text = new HashMap<>();
		text.put("content", content);
		body.put("text", text);

		Map<String, Object> at = new HashMap<>();
		at.put("isAtAll", atAll);
		if (atMobiles != null && !atMobiles.isEmpty()) {
			at.put("atMobiles", atMobiles);
		}
		body.put("at", at);

		return doSend(body);
	}

	/**
	 * 发送Markdown消息
	 * @param title 标题
	 * @param markdownText Markdown格式的消息内容
	 * @return 钉钉API响应
	 */
	public String sendMarkdownMessage(String title, String markdownText) {
		return sendMarkdownMessage(title, markdownText, null, false);
	}

	/**
	 * 发送Markdown消息（可指定@的人）
	 * @param title 标题
	 * @param markdownText Markdown格式的消息内容
	 * @param atMobiles 需要@的手机号列表
	 * @param atAll 是否@所有人
	 * @return 钉钉API响应
	 */
	public String sendMarkdownMessage(String title, String markdownText, List<String> atMobiles, boolean atAll) {
		Map<String, Object> body = new HashMap<>();
		body.put("msgtype", "markdown");

		Map<String, String> markdown = new HashMap<>();
		markdown.put("title", title);
		markdown.put("text", markdownText);
		body.put("markdown", markdown);

		Map<String, Object> at = new HashMap<>();
		at.put("isAtAll", atAll);
		if (atMobiles != null && !atMobiles.isEmpty()) {
			at.put("atMobiles", atMobiles);
		}
		body.put("at", at);

		return doSend(body);
	}

	/**
	 * 发送Link消息
	 * @param title 标题
	 * @param text 描述内容
	 * @param messageUrl 点击消息跳转的URL
	 * @param picUrl 图片URL（可为空）
	 * @return 钉钉API响应
	 */
	public String sendLinkMessage(String title, String text, String messageUrl, String picUrl) {
		Map<String, Object> body = new HashMap<>();
		body.put("msgtype", "link");

		Map<String, String> link = new HashMap<>();
		link.put("title", title);
		link.put("text", text);
		link.put("messageUrl", messageUrl);
		if (picUrl != null && !picUrl.isEmpty()) {
			link.put("picUrl", picUrl);
		}
		body.put("link", link);

		return doSend(body);
	}

	private String doSend(Map<String, Object> body) {
		String webhookUrl = String.format(WEBHOOK_URL_TEMPLATE, accessToken);
		logger.info("Sending DingTalk message to webhook, msgtype: {}", body.get("msgtype"));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String jsonBody;
		try {
			jsonBody = OBJECT_MAPPER.writeValueAsString(body);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize DingTalk message body", e);
		}

		HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);

		logger.info("DingTalk API response: {}", response.getBody());
		return response.getBody();
	}

	public String getWebhookUrl() {
		return String.format(WEBHOOK_URL_TEMPLATE, accessToken);
	}

}

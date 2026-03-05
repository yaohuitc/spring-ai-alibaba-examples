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
package com.alibaba.cloud.ai.toolcall.component;

import com.alibaba.cloud.ai.toolcall.service.DingTalkService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class DingTalkTools {

	private final DingTalkService dingTalkService;

	public DingTalkTools(DingTalkService dingTalkService) {
		this.dingTalkService = dingTalkService;
	}

	@Tool(description = "Send a text message via DingTalk robot webhook. " +
			"Use this tool when you need to notify someone through DingTalk.")
	public String sendTextMessage(
			@ToolParam(description = "The text content of the message to send") String content) {
		return dingTalkService.sendTextMessage(content);
	}

	@Tool(description = "Send a Markdown formatted message via DingTalk robot webhook. " +
			"Use this tool when you need to send a rich-formatted report or notification through DingTalk.")
	public String sendMarkdownMessage(
			@ToolParam(description = "The title of the markdown message") String title,
			@ToolParam(description = "The markdown formatted content of the message") String markdownText) {
		return dingTalkService.sendMarkdownMessage(title, markdownText);
	}

	@Tool(description = "Send a link message via DingTalk robot webhook. " +
			"Use this tool when you need to share a link with title and description through DingTalk.")
	public String sendLinkMessage(
			@ToolParam(description = "The title of the link message") String title,
			@ToolParam(description = "The description text of the link") String text,
			@ToolParam(description = "The URL to navigate to when clicking the message") String messageUrl,
			@ToolParam(description = "The picture URL for the link message, can be empty") String picUrl) {
		return dingTalkService.sendLinkMessage(title, text, messageUrl, picUrl);
	}

}

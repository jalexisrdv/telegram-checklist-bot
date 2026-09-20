package com.jardvcode.bot.shared.domain.bot;

import com.jardvcode.bot.configuration.sender.MessageSender;
import com.jardvcode.bot.shared.domain.state.State;

import java.util.List;

public final class BotContext {
	
	private Long systemUserId;
	private Long botUserId;
	private final String platform;
	private final String providerUserId;
	private final String message;
	private Class<? extends State> previousState;
	private final MessageSender sender;
	
	public BotContext(String platform, String providerUserId, String message, MessageSender sender) {
        this.platform = platform;
        this.providerUserId = providerUserId;
		this.message = message;
		this.sender = sender;
	}
	
	public void sendText(String message) throws Exception {
		sender.sendText(providerUserId, message);
	}

	public void sendDocument(byte[] file, String fileName) throws Exception {
		sender.sendDocument(providerUserId, file, fileName);
	}

	public void sendActionMessage(String message, List<MessageAction> actions) throws Exception {
		sender.sendActionMessage(providerUserId, message, actions);
	}

	public Long getSystemUserId() {
		return systemUserId;
	}

	public void setSystemUserId(Long systemUserId) {
		this.systemUserId = systemUserId;
	}

	public Long getBotUserId() {
		return botUserId;
	}

	public void setBotUserId(Long botUserId) {
		this.botUserId = botUserId;
	}

	public String getPlatform() {
		return platform;
	}

	public String getProviderUserId() {
		return providerUserId;
	}

	public String getMessage() {
		return message;
	}

	public Class<? extends State> getPreviousState() {
		return previousState;
	}

	public void setPreviousState(Class<? extends State> previousState) {
		this.previousState = previousState;
	}

}

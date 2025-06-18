package com.toiukha.report.model;

import org.springframework.stereotype.Component;

@Component
public class ChatRequest {

	//定義前端傳來的請求格式
	 private String query;
	 private String conversation_id;

	 // Getters & Setters
	 public String getQuery() { return query; }
	 public void setQuery(String query) { this.query = query; }
	 public String getConversation_id() { return conversation_id; }
	 public void setConversation_id(String conversation_id) { this.conversation_id = conversation_id; }

	
}

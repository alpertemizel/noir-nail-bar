package com.alper.nailbar.dto.chat;

import jakarta.validation.constraints.NotBlank;

/**
 * Chatbot'a gönderilen istek DTO'su.
 * conversationId verilmezse yeni bir konuşma oturumu başlatılır.
 */
public class ChatRequest {

    @NotBlank(message = "Mesaj boş olamaz.")
    private String message;

    private String conversationId; // Opsiyonel — mevcut oturumu sürdürmek için

    public ChatRequest() {
    }

    public ChatRequest(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    // Getter ve Setter Metotları
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}

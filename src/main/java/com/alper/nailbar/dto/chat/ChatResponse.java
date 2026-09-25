package com.alper.nailbar.dto.chat;

/**
 * Chatbot yanıt DTO'su.
 * reply: AI'ın ürettiği yanıt metni.
 * conversationId: İstemcinin sonraki isteklerde oturumu sürdürmek için kullanacağı kimlik.
 */
public class ChatResponse {

    private String reply;
    private String conversationId;

    public ChatResponse() {
    }

    public ChatResponse(String reply, String conversationId) {
        this.reply = reply;
        this.conversationId = conversationId;
    }

    // Getter ve Setter Metotları
    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}

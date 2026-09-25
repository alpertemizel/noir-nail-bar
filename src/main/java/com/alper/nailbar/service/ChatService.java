package com.alper.nailbar.service;

import com.alper.nailbar.dto.chat.ChatRequest;
import com.alper.nailbar.dto.chat.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * AI Chatbot servis katmanı.
 * <p>
 * Spring AI {@link ChatClient} kullanarak OpenAI ile iletişim kurar.
 * Her konuşma oturumu bir {@code conversationId} ile takip edilir;
 * bu ID üzerinden {@link ChatMemory} önceki mesajları otomatik olarak prompt'a ekler.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Kullanıcı mesajını AI'a iletir ve yanıtı döndürür.
     * <p>
     * conversationId null/boş ise yeni bir oturum başlatılır.
     * Mevcut bir conversationId verilirse o oturumdaki konuşma geçmişi korunur.
     *
     * @param request Kullanıcının chat isteği (mesaj + opsiyonel conversationId)
     * @return AI yanıtı ve oturum kimliği
     */
    public ChatResponse chat(ChatRequest request) {
        // Oturum kimliği: istemciden geldiyse koru, yoksa yeni üret
        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }
        final String sessionId = conversationId;

        try {
            // ChatClient üzerinden AI çağrısı — advisor conversationId ile geçmişi yönetir
            String reply = chatClient.prompt()
                    .user(request.getMessage())
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .content();

            return new ChatResponse(reply, sessionId);

        } catch (Exception ex) {
            log.error("AI chatbot çağrısında hata oluştu [sessionId={}]: {}", sessionId, ex.getMessage(), ex);

            // Hatanın kök nedenine göre kullanıcı dostu mesaj üret
            String errorMessage = resolveErrorMessage(ex);
            return new ChatResponse(errorMessage, sessionId);
        }
    }

    /**
     * Exception türüne ve mesajına göre kullanıcı dostu hata mesajı döndürür.
     */
    private String resolveErrorMessage(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (msg.contains("api key") || msg.contains("unauthorized") || msg.contains("401")) {
            return "Üzgünüz, yapay zeka servisimizde bir yapılandırma sorunu var. "
                    + "Lütfen daha sonra tekrar deneyiniz veya doğrudan stüdyomuzla iletişime geçiniz.";
        }
        if (msg.contains("rate limit") || msg.contains("429") || msg.contains("quota")) {
            return "Şu anda yoğun talep nedeniyle yapay zeka asistanımız geçici olarak kullanılamıyor. "
                    + "Lütfen birkaç dakika sonra tekrar deneyiniz.";
        }
        if (msg.contains("timeout") || msg.contains("timed out") || msg.contains("connection")) {
            return "Yapay zeka servisine bağlantı kurulamadı. "
                    + "Lütfen internet bağlantınızı kontrol edip tekrar deneyiniz.";
        }

        return "Yapay zeka asistanımız şu anda yanıt veremiyor. "
                + "Lütfen daha sonra tekrar deneyiniz veya doğrudan stüdyomuzla iletişime geçiniz.";
    }
}


package com.alper.nailbar.controller;

import com.alper.nailbar.dto.chat.ChatRequest;
import com.alper.nailbar.dto.chat.ChatResponse;
import com.alper.nailbar.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Chatbot REST endpoint'i.
 * <p>
 * Müşteriler bu endpoint üzerinden chatbot ile konuşabilir.
 * Herkese açıktır — kimlik doğrulama gerektirmez.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Müşterinin mesajını AI chatbot'a iletir ve yanıtı döndürür.
     * <p>
     * İstek gövdesi:
     * <pre>
     * {
     *   "message": "Manikür hizmetiniz var mı?",
     *   "conversationId": "uuid-string"  // opsiyonel — ilk mesajda boş bırakılır
     * }
     * </pre>
     *
     * Yanıt gövdesi:
     * <pre>
     * {
     *   "reply": "Evet, manikür hizmetimiz mevcuttur...",
     *   "conversationId": "uuid-string"  // sonraki mesajlarda oturumu sürdürmek için kullanılır
     * }
     * </pre>
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }
}

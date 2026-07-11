package org.hbrs.se2.project.hellocar.control;

import com.vaadin.flow.component.UI;
import jakarta.transaction.Transactional;
import org.hbrs.se2.project.hellocar.services.ai.AIService;
import org.hbrs.se2.project.hellocar.util.Globals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ChatControl {

    private static final String CONTENT_KEY = "content";

    private static final String CHATBOT_FEHLER_TEXT =
            "Der Chatbot ist gerade leider nicht verfügbar. Bitte versuche es später nochmal.";

    @Autowired
    private PromptControl promptControl;

    @Autowired
    private AIService aiService;

    public boolean sendAndGetMessage(String userMessage) {

        if (userMessage == null || userMessage.isBlank()) {
            return false;
        }

        List<Map<String, String>> chatHistory = getCurrentChatHistory();

        // Tokens Sparen, an History
        while (chatHistory.size() > 21) {
            chatHistory.remove(1);
        }

        chatHistory.add(Map.of("role", "user", CONTENT_KEY, userMessage));

        String response;

        try {
            response = this.aiService.sendMessageAndGetResponse(chatHistory);
        } catch (Exception e) {
            // FIXX! Die Anwendung soll nicht abstürzen, nur weil KI nicht erreichbar.
            System.out.println("Chatbot konnte keine Antwort liefern: " + e.getMessage());
            response = CHATBOT_FEHLER_TEXT;
        }

        chatHistory.add(Map.of("role", "assistant", CONTENT_KEY, response));

        UI.getCurrent().getSession().setAttribute(Globals.CHAT_HISTORY, chatHistory);

        return true;
    }

    @Transactional
    public List<Map<String, String>> getCurrentChatHistory() {

        List<Map<String, String>> chatHistory =
                (List<Map<String, String>>) UI.getCurrent().getSession().getAttribute(Globals.CHAT_HISTORY);

        if (chatHistory == null) {
            chatHistory = new ArrayList<>();

            String prompt;

            try {
                prompt = this.promptControl.getSystemPrompt();
            } catch (Exception e) {
                // Fallback, falls beim Promptbau z.B. die DB gerade nicht erreichbar ist.
                System.out.println("System-Prompt konnte nicht geladen werden: " + e.getMessage());
                prompt = "Du bist ein hilfreicher Assistent für Gründungsideen und Bewerbungen.";
            }

            chatHistory.add(Map.of("role", "system", CONTENT_KEY, prompt));

            chatHistory.add(Map.of("role", "assistant", CONTENT_KEY,
                    "Hi, mein Name ist Startito. Ich bin dein Chatbot für Gründungsideen und Bewerbungen."));

            UI.getCurrent().getSession().setAttribute(Globals.CHAT_HISTORY, chatHistory);
        }

        return chatHistory;
    }
}

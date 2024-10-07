package io.github.glandais.npbot.npbot;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
public class StuffController {

    private final ChatClient chatClient;

    @Value("classpath:/prompts/qa-prompt.st")
    private Resource qaPromptResource;

    private String context;

    @Autowired
    public StuffController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/")
    public String respond(@RequestParam(value = "message") String message) throws IOException {
        PromptTemplate promptTemplate = new PromptTemplate(qaPromptResource);
        Map<String, Object> map = new HashMap<>();
        map.put("question", message);
        map.put("context", getContext());
        Prompt prompt = promptTemplate.create(map);
        ChatClient.CallPromptResponseSpec call = chatClient.prompt(prompt).call();
        ChatResponse chatResponse = call.chatResponse();
        Generation generation = chatResponse.getResult();
        AssistantMessage output = generation.getOutput();
        String content = output.getContent();
        return content;
    }

    private String getContext() throws IOException {
        if (context == null) {
            synchronized (this) {
                if (context == null) {
                    try (InputStream fis = StuffController.class.getResourceAsStream("/docs/wiki.zip")) {
                        context = MarkdownAggregator.readMarkdownFromZip(fis);
                    }
                }
            }
        }
        return context;
    }

}

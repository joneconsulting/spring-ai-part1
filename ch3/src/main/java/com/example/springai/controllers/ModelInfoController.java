package com.example.springai.controllers;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ModelInfoController {

    private final ChatModel chatModel;

    @Value("${spring.ai.openai.chat.model:기본값 사용}")
    private String modelName;

    public ModelInfoController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/model")
    public Map<String, String> getModelInfo() {
        if (chatModel instanceof OpenAiChatModel openAiChatModel) {
            modelName = openAiChatModel.getOptions().getModel();
            System.out.println("현재 실제 사용 모델: " + modelName);
        }

        return Map.of(
                "chatModelClass", chatModel.getClass().getName(),
                "modelName", modelName
        );
    }
}

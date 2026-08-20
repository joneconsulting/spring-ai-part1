package com.example.springai.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OpenAIServiceImplTest {

    @Autowired
    OpenAIServiceImpl openAIService;

    @Test
    void getAnswer() {
//        String answer = openAIService.getAnswer("What is the meaning of life?");
//        String answer = openAIService.getAnswer("생명의 의미는 무엇인가?");
        String answer = openAIService.getAnswer("Wrtie a python script to output numbers from 1 to 100");

        System.out.println(answer);
    }
}
package com.example.springai.services;

import com.example.springai.model.Answer;
import com.example.springai.model.Question;

public interface OpenAIService {

    String getAnswer(String question);

    Answer getAnswer(Question question);
}

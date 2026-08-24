package com.example.springai.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MovieServiceImpl implements MovieService {
    private final ChatClient chatClient;

    public MovieServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public Movie findMovie(String title) {
        return null;
    }

    public List<Movie> findMoviesByDirector(String director, int count) {
        return null;
    }

    public String askRawJsonTrap(String title) {
        return null;
    }

    public String askRawJsonFixed(String title) {
        return null;
    }

    /**
     * [S10 강의 노트용] entity()가 프롬프트 뒤에 자동으로 붙이는
     * 포맷 지시문(JSON 스키마)을 문자열로 반환합니다.
     * "마법이 아니라 자동화"임을 보여줄 때 사용하세요.
     */
    public String describeFormat() {
        return new BeanOutputConverter<>(Movie.class).getFormat();
    }
}

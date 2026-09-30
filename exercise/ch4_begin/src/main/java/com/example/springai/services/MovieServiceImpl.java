package com.example.springai.services;

import com.example.springai.jev.JevClient;
import com.example.springai.model.Genre;
import com.example.springai.model.Movie;
import com.example.springai.model.ReviewAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MovieServiceImpl implements MovieService {
    private final ChatClient chatClient;

    public MovieServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public Movie findMovie(String title) {
        return chatClient.prompt()
                .user(u -> u.text("영화 '{title}'의 제목, 개봉연도, 감독을 알려줘").param("title", title))
                .call()
                .entity(Movie.class);
    }

    public List<Movie> findMoviesByDirector(String director, int count) {
        return chatClient.prompt()
                .user(u -> u.text("{director} 감독의 대표작 {count}편을 알려줘")
                        .param("director", director)
                        .param("count", String.valueOf(count)))
                .call()
                .entity(new ParameterizedTypeReference<List<Movie>>() {});
    }

    /* 문자열로 JSON을 직접 요청하는 방식
    * ```json ... ``` */
    public String askRawJsonTrap(String title) {
        String raw = chatClient.prompt()
                .user(u -> u.text("영화 '{title}'의 정보를 title, year, director 필드를 가진 JSON으로 알려줘")
                        .param("title", title))
                .call()
                .content();

        log.info("TRAP RAW >>> {}", raw);

        return raw;
    }

    /* 프롬프트에 마크다운 금지를 명시 */
    public String askRawJsonFixed(String title) {
        String raw = chatClient.prompt()
                .user(u -> u.text("""
                                영화 '{title}'의 정보를 title, year, director 필드를 가진 JSON으로 알려줘.
                                Response in JSON format without markdown tags.
                                """)
                        .param("title", title))
                .call()
                .content();

        log.info("FIXED RAW >>> {}", raw);

        return raw;
    }

    /**
     * [S10 강의 노트용] entity()가 프롬프트 뒤에 자동으로 붙이는
     * 포맷 지시문(JSON 스키마)을 문자열로 반환합니다.
     * "마법이 아니라 자동화"임을 보여줄 때 사용하세요.
     */
    public String describeFormat() {
        return new BeanOutputConverter<>(Movie.class).getFormat();
    }

    /**
     * [S10-대응②와 비교] LLM + entity() — 스키마를 "부탁"하는 방식.
     * 대부분 잘 되지만 enum에 없는 값이 오면 파싱 예외가 날 수 있습니다.
     */
    public ReviewAnalysis analyzeReviewWithLlm(String review) {
        return null;
    }

    /**
     * [S10-대응③] Jev — 보기 중에서만 고르므로 형식 오류가 구조적으로 불가능.
     * criteria를 enum에서 만들기 때문에 응답은 항상 Genre.valueOf()로 변환됩니다.
     */
    public ReviewAnalysis analyzeReviewWithJev(String review) {
        Map<String, String> genreCriteria = new LinkedHashMap<>();
        for (Genre g : Genre.values()) {
            genreCriteria.put(g.name(), g.description());
        }

        Map<String, JevClient.Decision> answers = null;

        JevClient.Decision genre = answers.get("genre");

        return null;
    }
}

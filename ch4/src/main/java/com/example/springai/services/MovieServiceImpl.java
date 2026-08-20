package com.example.springai.services;

import com.example.springai.model.Movie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MovieServiceImpl implements MovieService {
    private final ChatClient chatClient;

    public MovieServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * [S8] entity() — 한 줄 타입 매핑.
     * record를 넘기면 스키마 지시문 삽입 + JSON 파싱까지 자동입니다.
     */
    public Movie findMovie(String title) {
        return chatClient.prompt()
                .user(u -> u.text("영화 '{title}'의 제목, 개봉연도, 감독을 알려줘")
                        .param("title", title))
                .call()
                .entity(Movie.class);
    }

    /**
     * [S9] 컬렉션 매핑 — 제네릭은 Type Erasure 때문에
     * ParameterizedTypeReference로 타입 정보를 보존해 전달합니다.
     * (RestClient에서 이미 쓰던 패턴 그대로)
     */
    public List<Movie> findMoviesByDirector(String director, int count) {
        List<Movie> movies = chatClient.prompt()
                .user(u -> u.text("{director} 감독의 대표작 {count}편을 알려줘")
                        .param("director", director)
                        .param("count", String.valueOf(count)))
                .call()
                .entity(new ParameterizedTypeReference<List<Movie>>() {});

        movies.forEach(m -> System.out.println(m.title() + " (" + m.year() + ")"));

        return movies;
    }

    /**
     * [S10-함정] 문자열로 JSON을 직접 요청하는 방식.
     * 모델이 ```json ... ``` 코드펜스로 감싸 반환하는 경우가 있어
     * 그대로 Jackson 파싱을 시도하면 깨집니다. → 응답 원문을 눈으로 확인하세요.
     * compact(한 줄 압축)
     */
    public String askRawJsonTrap(String title) {
        String raw = chatClient.prompt()
                .user(u -> u.text("영화 '{title}'의 정보를 title, year, director 필드를 가진 JSON으로 알려줘")
                        .param("title", title))
                .call()
                .content();

        log.info("TRAP RAW >>>\n{}", raw);

        return raw;
    }

    /**
     * [S10-대응①] 프롬프트에 마크다운 금지를 명시하는 방식.
     * (대응②는 애초에 entity()를 사용하는 것 — findMovie 메서드)
     * pretty-print(사람이 읽기 좋은 여러 줄 형식)
     */
    public String askRawJsonFixed(String title) {
        String raw = chatClient.prompt()
                .user(u -> u.text("""
                            영화 '{title}'의 정보를 title, year, director 필드를 가진 JSON으로 알려줘.
                            Respond in JSON format without markdown tags.
                            """)
                        .param("title", title))
                .call()
                .content();

        log.info("FIXED RAW >>>\n{}", raw);

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
}

package com.example.springai.controllers;

import com.example.springai.model.Movie;
import com.example.springai.services.MovieService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /** [S8] entity() 단건 매핑. 예) GET /api/movies/one?title=기생충 */
    @GetMapping("/one")
    public Movie one(@RequestParam(defaultValue = "기생충") String title) {
        return movieService.findMovie(title);
    }

    /** [S9] List 매핑. 예) GET /api/movies/by-director?director=봉준호&count=3 */
    @GetMapping("/by-director")
    public List<Movie> byDirector(@RequestParam(defaultValue = "봉준호") String director,
                                  @RequestParam(defaultValue = "3") int count) {
        return movieService.findMoviesByDirector(director, count);
    }

    /** [10-함정] 마크다운 코드펜스로 감싸져 올 수 있음. 여러 번 호출해 관찰하세요. */
    @GetMapping("/raw-json-trap")
    public String rawJsonTrap(@RequestParam(defaultValue = "기생충") String title) {
        return movieService.askRawJsonTrap(title);
    }

    /** [S10-대응] "without markdown tags" 명시 버전 */
    @GetMapping("/raw-json-fixed")
    public String rawJsonFixed(@RequestParam(defaultValue = "기생충") String title) {
        return movieService.askRawJsonFixed(title);
    }

    /** [S8~S10 노트] entity()가 자동 주입하는 포맷 지시문(JSON 스키마) 확인 */
    @GetMapping("/format")
    public String format() {
        return movieService.describeFormat();
    }

}


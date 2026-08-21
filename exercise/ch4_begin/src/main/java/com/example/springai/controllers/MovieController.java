package com.example.springai.controllers;

import com.example.springai.services.MovieService;
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

    @GetMapping("/one")
    public Movie one(@RequestParam(defaultValue = "기생충") String title) {

    }

    @GetMapping("/by-director")
    public List<Movie> byDirector(@RequestParam(defaultValue = "봉준호") String director,
                                  @RequestParam(defaultValue = "3") int count) {

    }

    @GetMapping("/raw-json-trap")
    public String rawJsonTrap(@RequestParam(defaultValue = "기생충") String title) {

    }

    @GetMapping("/raw-json-fixed")
    public String rawJsonFixed(@RequestParam(defaultValue = "기생충") String title) {

    }

    /** [S8~S10 노트] entity()가 자동 주입하는 포맷 지시문(JSON 스키마) 확인 */
    @GetMapping("/format")
    public String format() {
        return movieService.describeFormat();
    }

}


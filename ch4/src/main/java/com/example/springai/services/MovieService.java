package com.example.springai.services;

import com.example.springai.model.Movie;

import java.util.List;

public interface MovieService {

    Movie findMovie(String title);

    List<Movie> findMoviesByDirector(String director, int count);

    String askRawJsonTrap(String title);

    String askRawJsonFixed(String title);

    String describeFormat();
}

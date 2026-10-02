package com.example.springai.model;

public enum Genre {
    DRAMA("인물과 관계 중심의 드라마"),
    THRILLER("긴장감과 반전 중심의 스릴러"),
    COMEDY("웃음을 주는 코미디"),
    ACTION("액션과 볼거리 중심"),
    SF("과학·미래 설정의 SF"),
    ROMANCE("사랑 이야기 중심의 로맨스");

    private final String description;
    Genre(String description) { this.description = description; }
    public String description() { return description; }
}
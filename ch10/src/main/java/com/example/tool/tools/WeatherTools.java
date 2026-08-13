package com.example.tool.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 실습 1 — 날씨 조회 Tool (외부 API 대체) — 교안 10장 3절
 *
 * 실제 외부 API 대신 결정적인 가짜 데이터를 반환한다.
 * (강의에서 실 API를 붙일 때는 여기서 RestClient/WebClient 호출로 교체)
 *
 * 핵심 학습: @Tool 의 description 은 "기계가 읽는 명세"다.
 *          LLM 은 이 문장만 보고 도구를 고른다.
 */
@Component
public class WeatherTools {

    private static final Logger log = LoggerFactory.getLogger(WeatherTools.class);

    // 데모용 고정 데이터 (지식 컷오프 이후의 "실시간 정보"를 흉내낸다)
    private static final Map<String, String> FAKE_WEATHER = Map.of(
            "서울", "맑음, 기온 3도, 미세먼지 나쁨",
            "부산", "흐림, 기온 8도, 미세먼지 보통",
            "제주", "비, 기온 11도, 강풍 주의보"
    );

    @Tool(description = "지정한 한국 도시의 현재 날씨를 조회한다. 도시명은 '서울', '부산', '제주' 중 하나여야 한다.")
    public String getCurrentWeather(
            @ToolParam(description = "날씨를 조회할 도시명. 예: 서울") String city) {

        log.info("[Tool 호출] getCurrentWeather(city={})", city);   // 관측성 (교안 10장 4절)
        String weather = FAKE_WEATHER.get(city.trim());

        if (weather == null) {
            // 실패 시 폴백: LLM 이 처리할 수 있도록 명확한 메시지 반환
            return "'%s'의 날씨 정보를 찾을 수 없습니다. 서울, 부산, 제주만 조회 가능합니다.".formatted(city);
        }
        return "%s의 현재 날씨: %s".formatted(city, weather);
    }
}

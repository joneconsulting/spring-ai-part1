package com.example.springai.jev;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

//@Component
public class JevClient {
    private final RestClient restClient;
    private final JevProperties props;

    public JevClient(JevProperties props) {
        this.props = props;
        this.restClient = null;
    }

    public boolean enabled() {
        return props.enabled();
    }

    /** state 하나에 여러 질문을 보내고, 질문 key별 결정을 돌려받습니다. */
    public Map<String, Decision> decide(Object state, Map<String, Object> questions) {

        return null;
    }

    public record JevResponse(String model, Map<String, Decision> answers) {
    }

    /** 질문 타입에 따라 채워지는 필드가 다릅니다 (choice / score / noul). */
    public record Decision(String type, String choice, Double score, String recommend, Double noul,
                           Double confidence, Map<String, Double> probabilities) {
    }
}

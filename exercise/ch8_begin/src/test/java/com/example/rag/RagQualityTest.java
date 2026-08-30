package com.example.rag;

import com.example.rag.eval.EvaluationService;
import com.example.rag.eval.RagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 품질 회귀 감지기 (교안 8장 7절)
 *
 * 실행:  ./gradlew test -Peval
 *   -> LLM 호출 비용이 발생하므로 기본 test 태스크에서는 제외된다.
 */
@Tag("eval")
@SpringBootTest
class RagQualityTest {

    @Autowired ChatClient.Builder builder;
    @Autowired RagService ragService;
    @Autowired EvaluationService evaluationService;

    @Test
    @DisplayName("연차 질문은 근거에 기반해 답해야 한다")
    void 연차_질문은_근거에_기반해_답해야_한다() {
        String question = "연차 유급휴가는 며칠인가요?";

        // (1) RAG 실행
        // (2) 근거가 검색되었는가

        // (3) 평가기
        // (4) LLM 판정

        // (5) 판정
    }

    @Test
    @DisplayName("문서에 없는 내용은 지어내지 않아야 한다 (환각 방어)")
    void 문서에_없는_내용은_지어내지_않아야_한다() {

    }

    @Test
    @DisplayName("전체 평가 세트 통과율이 기준선 이상이어야 한다")
    void 전체_평가세트_통과율_기준선() {

        // 기준선: 15문항 중 11문항(약 73%) 이상 통과

    }
}

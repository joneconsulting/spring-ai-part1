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
 *
 * 의미: 일반 테스트가 "코드 회귀"를 잡듯이, 이 테스트는 "품질 회귀"를 잡는다.
 *      청킹을 바꿨는데 이 테스트가 깨지면 개선이 아니라 파괴였다는 뜻이다.
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

        RagService.RagResult result = ragService.ask(question);          // (1) RAG 실행
        assertThat(result.evidences()).isNotEmpty();                     // (2) 근거가 검색되었는가

        var evaluator = new RelevancyEvaluator(builder);                 // (3) 평가기
        EvaluationRequest req =
                new EvaluationRequest(question, result.evidences(), result.answer());
        EvaluationResponse res = evaluator.evaluate(req);                // (4) LLM 판정

        assertThat(res.isPass()).isTrue();                               // (5) 판정
    }

    @Test
    @DisplayName("문서에 없는 내용은 지어내지 않아야 한다 (환각 방어)")
    void 문서에_없는_내용은_지어내지_않아야_한다() {
        RagService.RagResult result = ragService.ask("회사 주차장은 몇 대까지 수용하나요?");
        assertThat(result.answer()).contains("확인할 수 없습니다");
    }

    @Test
    @DisplayName("전체 평가 세트 통과율이 기준선 이상이어야 한다")
    void 전체_평가세트_통과율_기준선() {
        EvaluationService.Report report = evaluationService.run();
        System.out.println("=== 평가 리포트 ===");
        System.out.println("전체 통과율    : " + report.overallPassRate());
        System.out.println("유형별 통과율  : " + report.passRateByType());
        System.out.println("평균 근거 토큰 : " + report.avgContextTokensApprox());
        System.out.println("실패 문항      : " + report.failedIds());

        // 기준선: 15문항 중 11문항(약 73%) 이상 통과
        long passed = report.items().stream().filter(EvaluationService.ItemResult::pass).count();
        assertThat(passed).isGreaterThanOrEqualTo(11);
    }
}

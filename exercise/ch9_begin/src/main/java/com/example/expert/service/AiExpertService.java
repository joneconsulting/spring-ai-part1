package com.example.expert.service;

import com.example.expert.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Expert — 구조화 응답 x RAG 결합 (교안 9장 4절)
 *
 * 흐름:  조건 입력 -> 근거 검색 -> 구조화 판정 -> 근거 검증 -> HITL 라우팅
 *
 * QuestionAnswerAdvisor 를 쓰지 않는 이유:
 *   판단에서는 검색 결과를 프롬프트에 어떻게 넣을지 세밀하게 제어해야 하기 때문.
 */
@Service
public class AiExpertService {

    private static final Logger log = LoggerFactory.getLogger(AiExpertService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final EvidenceVerifier evidenceVerifier;
    private final int topK;
    private final int autoApproveScore;
    private final int reviewRequiredScore;

    public AiExpertService(ChatClient.Builder builder,
                           VectorStore vectorStore,
                           EvidenceVerifier evidenceVerifier,
                           @Value("10") int topK,
                           @Value("${expert.auto-approve-score:80}") int autoApproveScore,
                           @Value("${expert.review-required-score:50}") int reviewRequiredScore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.evidenceVerifier = evidenceVerifier;
        this.topK = topK;
        this.autoApproveScore = autoApproveScore;
        this.reviewRequiredScore = reviewRequiredScore;
    }

    public ExpertResponse recommend(Requirement req) {
        // ---------- 1) 조건을 질의로 만들어 근거 검색 ----------
        String query = "vCPU %d개, 메모리 %d GiB 스펙을 가진 %s 워크로드용 인스턴스"
                .formatted(req.vcpu(), req.memGb(), req.workload());

        List<Document> docs = null;

        if (docs.isEmpty()) {
            return null;
        } else {

        }

        // ---------- 2) 구조화 응답 + 근거 주입 ----------
        String context = null;

        Recommendation rec = chatClient.prompt()
                .system("""
                        너는 클라우드 아키텍트다. 아래 [규칙]만을 사용해 인스턴스를 추천하라.
                
                        규칙:
                        1. 근거에 없는 인스턴스나 요금을 절대 만들어내지 마라.
                        2. [근거]에는 요구사항과 무관한 인스턴스도 함께 포함되어 있을 수 있다.
                           그중 요구사항(vCPU, 메모리, 예산, 용도)에 가장 잘 부합하는
                           단 하나의 인스턴스를 선택해 그것을 기준으로 판단하라.
                           다른 인스턴스가 조건을 충족하지 못한다는 사실은 판단에 영향을 주지 않는다.
                        3. 요구사항을 충족하는 인스턴스가 [근거]에 전혀 없으면
                           decision 을 "판단 보류"로 하고 score 를 30 이하로 설정하라.
                        4. score 는 선택한 인스턴스 하나의 요구사항 충족도(0~100)다.
                           - 스펙과 예산을 모두 충족하면 80 이상
                           - 일부만 충족하면 50~79
                           - 충족하지 못하면 50 미만
                        5. evidences 는 [근거] 원문에 나온 문장/줄을 한 글자도 바꾸지 않고
                           그대로 인용해야 한다. 다음을 절대 하지 마라:
                           - 서로 떨어진 여러 줄을 하나로 이어붙이기
                           - 중간 줄을 생략한 채 앞뒤 줄만 이어붙이기
                           - 문장을 요약하거나 순서를 바꾸기
                           근거로 삼고 싶은 사실이 여러 줄에 걸쳐 있다면,
                           evidences 배열에 그 줄들을 각각 별도의 항목으로 나눠서 담아라.
                        6. reason 은 한국어 2~3문장으로 작성하라.""")
                .user("")
                .call()
                .entity(Recommendation.class);   // 구조화 응답으로 바로 매핑

        // ---------- 3) 근거 검증 (LLM이 인용을 지어냈는지) ----------


        // ---------- 4) HITL 라우팅 ----------




//        log.info("판정: {} (score={} -> {}, routing={}, 근거 {}건, 검증={})",
//                rec.decision(), rec.score(), effectiveScore, routing,
//                rec.evidences() == null ? 0 : rec.evidences().size(), verification.verified());
//
//        return new ExpertResponse(
//                rec.decision(), effectiveScore, rec.reason(), rec.evidences(),
//                verification.verified(), warnings, routing);
        return null;
    }

    /** 교안 10장 6절 - score 구간에 따른 HITL 라우팅 */
    private String route(int score) {
        if (score >= autoApproveScore) return "AUTO_APPROVE";      // 자동 진행
        if (score >= reviewRequiredScore) return "REVIEW_REQUIRED"; // 담당자 검토 큐
        return "HOLD";                                              // 판단 보류
    }

    private ExpertResponse hold(String reason, List<String> warnings) {
        return new ExpertResponse("판단 보류", 0, reason, List.of(), false, warnings, "HOLD");
    }
}

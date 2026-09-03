package com.example.dify.client;

import com.example.dify.session.ConversationSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Dify 채팅 서비스 — conversation_id 재사용을 캡슐화 (교안 10장 STEP 2)
 *
 * ★ 핵심: 우리 DB(여기서는 인메모리)에서 진행 중인 대화 ID 를 조회해 보내고,
 *   응답으로 받은 conversation_id 를 반드시 저장한다.
 *   이것을 하지 않으면 사용자는 "방금 말한 걸 왜 기억 못 하지?" 라고 느낀다.
 */
@Service
public class DifyChatService {

    private static final Logger log = LoggerFactory.getLogger(DifyChatService.class);

    private final DifyProxyClient difyClient;
    private final ConversationSessionRepository sessionRepo;

    public DifyChatService(DifyProxyClient difyClient,
                           ConversationSessionRepository sessionRepo) {
        this.difyClient = difyClient;
        this.sessionRepo = sessionRepo;
    }

    /** blocking 대화 — 대화 상태 유지 */
    public ChatResult chat(String userId, String question) {
        // ① 진행 중인 대화 조회 (없으면 새 대화)

        // ② 응답의 conversation_id 를 반드시 저장 (멀티턴의 핵심)

//        log.info("[Chat] user={}, convId '{}' -> '{}', mock={}, fallback={}",
//                userId, convId, reply.conversationId(), reply.mocked(), reply.fellBack());
//
//        return new ChatResult(reply.answer(), reply.conversationId(), reply.mocked(), reply.fellBack());
        return null;
    }

    /** streaming 대화 — SSE 프록시 */
    public Flux<String> chatStream(String userId, String question) {

        // 스트리밍은 종료 시점에 conversation_id 를 받으므로, 데모에서는 기존 convId 유지
        return null;
    }

    /** "새 대화 시작" */
    public void resetConversation(String userId) {
        sessionRepo.reset(userId);
    }

    public record ChatResult(String answer, String conversationId, boolean mocked, boolean fellBack) {}
}

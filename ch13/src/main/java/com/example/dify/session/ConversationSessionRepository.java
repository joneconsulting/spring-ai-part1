package com.example.dify.session;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * conversation_id 관리 (교안 13장 3절) ★
 *
 * Dify 의 대화 상태(conversation_id)를 우리 시스템의 사용자와 매핑한다.
 *
 * ※ 데모는 인메모리(ConcurrentHashMap)로 구현한다.
 *   교안 13장 3절의 지적대로, 실무에서는 인스턴스 스케일아웃에 대비해
 *   DB 또는 Redis 등 공유 저장소를 써야 한다.
 */
@Repository
public class ConversationSessionRepository {

    // userId -> Dify conversationId
    private final Map<String, String> store = new ConcurrentHashMap<>();

    /** 진행 중인 대화 ID 조회 (없으면 empty) */
    public Optional<String> findConversationId(String userId) {
        return Optional.ofNullable(store.get(userId));
    }

    /** 응답으로 받은 conversation_id 저장 */
    public void save(String userId, String conversationId) {
        if (conversationId != null && !conversationId.isBlank()) {
            store.put(userId, conversationId);
        }
    }

    /** "새 대화 시작" — 저장된 대화 ID 삭제 (교안 13장 3절) */
    public void reset(String userId) {
        store.remove(userId);
    }
}

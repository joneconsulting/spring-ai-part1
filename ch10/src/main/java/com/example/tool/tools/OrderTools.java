package com.example.tool.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 실습 2 — DB 조회 Tool (우리 시스템 데이터) — 교안 10장 3절
 *
 * ★ 핵심 원칙: "LLM 을 믿지 않는다"
 *   - 권한 검증은 Tool 메서드 안에서 한다 (교안 10장 4절)
 *   - 반환 데이터는 최소화하고 개인정보는 마스킹한다
 *
 * 실무에서는 SecurityContextHolder 로 인증 주체를 얻지만,
 * 데모에서는 application.yml 의 demo.current-user 로 대체한다.
 */
@Component
public class OrderTools {

    private static final Logger log = LoggerFactory.getLogger(OrderTools.class);

    private final String currentUser;

    // 데모용 주문 데이터: userId -> 주문 목록
    private static final Map<String, List<Order>> ORDER_DB = Map.of(
            "user-1024", List.of(
                    new Order("ORD-5501", "블루투스 키보드", "배송완료", "정*수"),
                    new Order("ORD-5588", "USB-C 허브", "배송중", "정*수")),
            "user-2048", List.of(
                    new Order("ORD-7701", "노트북 거치대", "결제완료", "김*희"))
    );

    public OrderTools(@Value("${demo.current-user}") String currentUser) {
        this.currentUser = currentUser;
    }

    @Tool(description = "현재 로그인한 사용자 본인의 최근 주문 내역과 배송 상태를 조회한다.")
    public String getMyOrders() {
        log.info("[Tool 호출] getMyOrders() - 인증 주체={}", currentUser);

        // ★ 권한 검증: LLM 이 넘긴 userId 를 신뢰하지 않고, 서버가 아는 인증 주체만 사용
        List<Order> orders = ORDER_DB.getOrDefault(currentUser, List.of());
        if (orders.isEmpty()) {
            return "주문 내역이 없습니다.";
        }
        return orders.stream()
                .map(o -> "- [%s] %s : %s".formatted(o.id(), o.product(), o.status()))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "주문번호로 특정 주문 1건의 배송 상태를 조회한다. 본인 주문만 조회할 수 있다.")
    public String getOrderStatus(
            @ToolParam(description = "조회할 주문번호. 예: ORD-5501") String orderId) {
        log.info("[Tool 호출] getOrderStatus(orderId={}) - 인증 주체={}", orderId, currentUser);

        // ★ 다른 사용자의 주문을 조회하려는 시도를 차단 (프롬프트 인젝션 방어)
        return ORDER_DB.getOrDefault(currentUser, List.of()).stream()
                .filter(o -> o.id().equalsIgnoreCase(orderId.trim()))
                .findFirst()
                .map(o -> "%s (%s): %s".formatted(o.id(), o.product(), o.status()))
                .orElse("주문번호 '%s'는 본인의 주문에서 찾을 수 없습니다.".formatted(orderId));
    }

    // 반환 시 수령인 이름은 이미 마스킹된 상태로 저장 (개인정보 최소화)
    private record Order(String id, String product, String status, String maskedReceiver) {}
}

package com.example.expert.domain;

/** 사용자 요구사항 (판단 시스템의 입력) */
public record Requirement(
        int vcpu,
        int memGb,
        int budget,        // 월 예산 (USD)
        String workload    // 워크로드 설명
) {
    public String describe() {
        return "vCPU %d개, 메모리 %dGB, 월 비용 %d USD, 용도: %s"
                .formatted(vcpu, memGb, budget, workload);
    }
}

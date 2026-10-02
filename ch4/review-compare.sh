#!/usr/bin/env bash
# ch4 리뷰 분석 비교: POST /api/movies/review/jev vs /review/llm
# 실행: ./review-compare.sh   (처음 한 번: chmod +x review-compare.sh)
# 다른 주소로 실행: BASE_URL=http://localhost:9090/api/movies/review ./review-compare.sh

BASE_URL="${BASE_URL:-http://localhost:8080/api/movies/review}"

# ${r:0:20}이 바이트가 아니라 글자 단위로 자르도록 UTF-8 로케일 지정 (한글 깨짐 방지)
export LC_ALL=en_US.UTF-8

REVIEWS=(
  "마지막 반전에서 범인이 사실 주인공의 형이었다는 게 밝혀질 때 소름이 돋았다. 두 시간 내내 긴장을 놓을 수 없었던 최고의 스릴러."
  "웃기려고 애쓰는 게 너무 보여서 오히려 민망했다. 개그가 하나도 안 터지고 시간이 아까웠다."
  "우주 정거장과 시간 여행 설정이 정말 치밀하다. 영상미도 압도적이라 IMAX로 한 번 더 볼 생각이다."
  "두 사람이 결국 공항에서 헤어지고 10년 뒤 다시 만나는 결말은 뻔했지만 음악은 좋았다."
  "액션도 있고 로맨스도 있고 웃긴 장면도 있는데 딱히 뭐 하나 기억에 남진 않는다."
  "전형적인 범죄 느와르. 조직 보스의 몰락을 차갑게 그려 낸 수작이다."
)

for r in "${REVIEWS[@]}"; do
  echo "=== ${r:0:20}..."
  for m in jev llm; do
    printf "%-4s " "$m"
    curl -s -w "  (HTTP %{http_code}, %{time_total}s)\n" \
      -X POST "$BASE_URL/$m" \
      -H "Content-Type: text/plain; charset=UTF-8" \
      --data "$r"
  done
done

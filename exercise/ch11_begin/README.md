# Section 11 — 멀티모달: 이미지 · 음성 (실행 예제)

Spring AI의 **통일된 추상화**로 이미지·음성을 다룹니다.
`ChatModel`을 알면 나머지도 같은 패턴(주입받아 `call()`)이라는 것을 체감하는 것이 목적입니다.

## 준비

```bash
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```
> DB 불필요. 단, 모델 접근 권한이 필요합니다 (Vision·DALL·E·Whisper·TTS).
>> https://platform.openai.com/


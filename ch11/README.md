# Section 11 — 멀티모달: 이미지 · 음성 (실행 예제)

Spring AI의 **통일된 추상화**로 이미지·음성을 다룹니다.
`ChatModel`을 알면 나머지도 같은 패턴(주입받아 `call()`)이라는 것을 체감하는 것이 목적입니다.

## 준비

```bash
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```
> DB 불필요. 단, 모델 접근 권한이 필요합니다 (Vision·DALL·E·Whisper·TTS).

---

## 실행

### 1) 이미지 생성 (ImageModel)
```bash
curl "localhost:8080/api/image?prompt=미니멀한 스프링 부트 로고, 녹색 계열, 플랫 디자인"
# {"url":"https://...","note":"URL 은 만료됩니다..."}
```
> 반환 URL은 만료되므로 실무에서는 즉시 저장합니다 (교안 11장 2절).

### 2) Vision — 이미지 설명
```bash
curl -X POST localhost:8080/api/vision/describe \
  -F "file=@/path/to/image.png"
```

### 3) Vision — 영수증 → 구조화 객체 ⭐ (이 섹션의 하이라이트)
```bash
curl -X POST localhost:8080/api/vision/receipt \
  -F "file=@/path/to/receipt.jpg"
```
```json
{
  "storeName": "○○카페",
  "date": "2025-11-01",
  "totalAmount": 12500,
  "items": ["아메리카노", "카페라떼"]
}
```
**이미지 → 객체** 파이프라인이 완성됩니다. Section 4의 구조화 응답 + Vision의 결합입니다.

### 4) STT — 음성 → 텍스트
```bash
curl -X POST localhost:8080/api/stt \
  -F "file=@/path/to/audio.mp3"
```
> `language="ko"`를 명시했습니다 — 한국어 인식률이 눈에 띄게 개선됩니다 (교안 11장 4절).

### 5) TTS — 텍스트 → 음성
```bash
curl "localhost:8080/api/tts?text=안녕하세요. 스프링 AI 음성 합성 예제입니다." \
  --output speech.mp3
```

---

## 테스트 파일 준비

실습용 이미지·음성이 필요합니다. 아래처럼 직접 준비하세요.

- **이미지**: 스마트폰으로 영수증을 찍거나, 아무 PNG/JPG 파일
- **음성**: 스마트폰 음성 메모로 한국어 몇 문장 녹음 (mp3/m4a/wav)
  또는 `/api/tts`로 만든 `speech.mp3`를 `/api/stt`에 다시 넣어 왕복 테스트

```bash
# TTS → STT 왕복 테스트 (별도 파일 없이 검증)
curl "localhost:8080/api/tts?text=연차 유급휴가는 십오일입니다" --output roundtrip.mp3
curl -X POST localhost:8080/api/stt -F "file=@roundtrip.mp3"
# → "연차 유급휴가는 15일입니다" 유사하게 인식되면 성공
```

---

## 핵심 메시지 — 추상화의 일관성

| 모델 | 변환 | 사용법 |
|---|---|---|
| `ChatModel` | 텍스트 → 텍스트 | `prompt().user().call()` |
| `ImageModel` | 텍스트 → 이미지 | `call(new ImagePrompt(...))` |
| `OpenAiAudioTranscriptionModel` | 음성 → 텍스트 | `call(new AudioTranscriptionPrompt(...))` |
| `OpenAiAudioSpeechModel` | 텍스트 → 음성 | `call(text)` |

**이름과 Prompt 타입만 다를 뿐 패턴이 같습니다.**

## 실습 과제

1. **콜센터 파이프라인 스케치** (교안 11장 6절): `/api/stt`로 통화 녹취를 텍스트화한 뒤,
   그 텍스트를 Section 7의 RAG API에 넣어 "매뉴얼 근거로 응대 품질 평가"를 이어보기
2. **비용 관찰**: 큰 이미지와 작은 이미지의 Vision 응답 시간·비용 차이 (해상도에 비례)

## 주요 파일
```
src/main/java/com/example/multimodal/
├── service/ImageGenService.java   # 이미지 생성
├── service/VisionService.java     # Vision + 영수증 구조화 추출 ★
├── service/AudioService.java      # STT(ko) / TTS
└── api/MultimodalController.java
```

---
### 주의
- **Spring AI 1.0.0 GA 기준**. `media()`, `OpenAiImageOptions`, `OpenAiAudioTranscriptionOptions`,
  Speech 모델의 호출 시그니처는 버전 확인 필요 (교안 `[Week 0 검증 대상]`).
- 멀티모달 모델은 **비용이 큽니다**. 이미지 1장 = 텍스트 수천 토큰 수준.
- 개인정보가 담긴 이미지·음성은 마스킹·동의 여부를 확인하세요 (교안 11장 6절 / Sec 14).

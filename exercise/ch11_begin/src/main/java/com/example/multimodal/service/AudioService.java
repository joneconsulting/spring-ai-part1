package com.example.multimodal.service;

import com.openai.models.audio.AudioResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * STT / TTS (교안 11장 4~5절)
 *
 * STT 핵심: language 를 "ko" 로 명시하면 한국어 인식률이 눈에 띄게 개선된다.
 *          텍스트가 되는 순간, 앞에서 배운 RAG/구조화 판정이 모두 적용된다.
 */
@Service
public class AudioService {

    private static final Logger log = LoggerFactory.getLogger(AudioService.class);

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final OpenAiAudioSpeechModel speechModel;

    public AudioService(OpenAiAudioTranscriptionModel transcriptionModel,
                        OpenAiAudioSpeechModel speechModel) {
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
    }

    /** STT — 음성을 텍스트로 */
    public String transcribe(Resource audio) {
        log.info("[STT] transcribe - {}", audio.getFilename());

//        var options = null;

        return null;
    }

    /** TTS — 텍스트를 음성으로 (MP3 바이트) */
    public byte[] speak(String text) {
        log.info("[TTS] speak - {}자", text.length());
        return speechModel.call(text);
    }
}

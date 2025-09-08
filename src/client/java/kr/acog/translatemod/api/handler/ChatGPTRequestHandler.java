package kr.acog.translatemod.api.handler;

import kr.acog.translatemod.api.TranslateHandler;

import java.util.concurrent.CompletableFuture;

public class ChatGPTRequestHandler {

    public static CompletableFuture<String> request(TranslateHandler.TranslateData data) {
        return null;
    }

    public static String extractFromResponseBody(String body) {
        return "Translated text from ChatGPT response";
    }

}

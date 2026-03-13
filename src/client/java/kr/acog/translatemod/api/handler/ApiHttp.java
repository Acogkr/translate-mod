package kr.acog.translatemod.api.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class ApiHttp {

    static final HttpClient CLIENT = HttpClient.newHttpClient();
    static final ObjectMapper MAPPER = new ObjectMapper();

    static CompletableFuture<String> sendJsonRequest(
            String url,
            Map<String, String> headers,
            ObjectNode body,
            Function<JsonNode, String> responseExtractor,
            String providerName
    ) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)));

            headers.forEach(builder::header);

            return CLIENT.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            throw new RuntimeException(providerName + " 오류: " + response.statusCode() + " " + response.body());
                        }
                        try {
                            return responseExtractor.apply(MAPPER.readTree(response.body()));
                        } catch (Exception e) {
                            throw new RuntimeException("응답 파싱 실패", e);
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

}

package backend.academy.bot.service.push;

import backend.academy.bot.config.properties.PushProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmHttpPushSender implements PushSender {
    private final PushProperties pushProperties;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Override
    public PushDeliveryResult send(String token, PushMessagePayload payload) {
        String serverKey = pushProperties.fcm().serverKey();
        String endpoint = pushProperties.fcm().endpoint();
        if (serverKey == null || serverKey.isBlank()) {
            return PushDeliveryResult.permanentFailure("missing_server_key");
        }
        try {
            String body = objectMapper.writeValueAsString(new LegacyFcmRequest(token, "high", payload.metadata()));
            String responseBody = webClientBuilder
                    .build()
                    .post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "key=" + serverKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            LegacyFcmResponse response = objectMapper.readValue(responseBody, LegacyFcmResponse.class);
            if (response.failure() == 0) {
                return PushDeliveryResult.success();
            }
            String errorReason = response.results().isEmpty()
                    ? "unknown_failure"
                    : response.results().getFirst().error();
            if ("NotRegistered".equals(errorReason) || "InvalidRegistration".equals(errorReason)) {
                return PushDeliveryResult.invalidToken(errorReason);
            }
            if ("Unavailable".equals(errorReason) || "InternalServerError".equals(errorReason)) {
                return PushDeliveryResult.transientFailure(errorReason);
            }
            return PushDeliveryResult.permanentFailure(errorReason == null ? "unknown_failure" : errorReason);
        } catch (Exception ex) {
            log.warn("FCM send failed due to transient error: {}", ex.getClass().getSimpleName());
            return PushDeliveryResult.transientFailure(ex.getClass().getSimpleName());
        }
    }

    private record LegacyFcmRequest(String to, String priority, Map<String, String> data) {}

    private record LegacyFcmResponse(int success, int failure, List<LegacyFcmResult> results) {}

    private record LegacyFcmResult(@JsonProperty("error") String error) {}
}

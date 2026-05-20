package backend.academy.bot.service.push;

import backend.academy.bot.config.properties.PushProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
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
    private final FcmAccessTokenProvider accessTokenProvider;

    @Override
    public PushDeliveryResult send(String token, PushMessagePayload payload) {
        String projectId = pushProperties.fcm().projectId();
        String endpointTemplate = pushProperties.fcm().endpointTemplate();
        if (projectId == null || projectId.isBlank()) {
            return PushDeliveryResult.permanentFailure("missing_project_id");
        }
        if (endpointTemplate == null || endpointTemplate.isBlank()) {
            return PushDeliveryResult.permanentFailure("missing_endpoint_template");
        }
        try {
            String endpoint = endpointTemplate.formatted(projectId);
            String body = objectMapper.writeValueAsString(new V1FcmRequest(new V1Message(token, payload.metadata())));
            String accessToken = accessTokenProvider.getAccessToken();

            FcmResponse response = webClientBuilder
                    .build()
                    .post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchangeToMono(clientResponse -> clientResponse
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(responseBody ->
                                    new FcmResponse(clientResponse.statusCode().value(), responseBody)))
                    .block();

            if (response == null) {
                return PushDeliveryResult.transientFailure("empty_response");
            }

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return PushDeliveryResult.success();
            }
            return mapFailure(response.statusCode(), response.body());
        } catch (Exception ex) {
            log.warn("FCM send failed due to transient error: {}", ex.getClass().getSimpleName());
            return PushDeliveryResult.transientFailure(ex.getClass().getSimpleName());
        }
    }

    private PushDeliveryResult mapFailure(int statusCode, String body) {
        FcmErrorEnvelope envelope = parseErrorEnvelope(body);
        String status =
                envelope != null && envelope.error() != null ? envelope.error().status() : null;
        String errorCode = extractFcmErrorCode(envelope);
        String message =
                envelope != null && envelope.error() != null ? envelope.error().message() : null;

        if ("UNREGISTERED".equals(errorCode) || "SENDER_ID_MISMATCH".equals(errorCode)) {
            return PushDeliveryResult.invalidToken(errorCode);
        }
        if ("INVALID_ARGUMENT".equals(status) && looksLikeTokenValidation(message, envelope)) {
            return PushDeliveryResult.invalidToken("INVALID_ARGUMENT_TOKEN");
        }
        if ("UNAVAILABLE".equals(status) || "INTERNAL".equals(status) || "QUOTA_EXCEEDED".equals(status)) {
            return PushDeliveryResult.transientFailure(status);
        }
        if (statusCode == 429 || statusCode >= 500) {
            return PushDeliveryResult.transientFailure(status != null ? status : "HTTP_" + statusCode);
        }
        return PushDeliveryResult.permanentFailure(status != null ? status : "HTTP_" + statusCode);
    }

    private FcmErrorEnvelope parseErrorEnvelope(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, FcmErrorEnvelope.class);
        } catch (Exception ex) {
            log.debug("Could not parse FCM v1 error body", ex);
            return null;
        }
    }

    private String extractFcmErrorCode(FcmErrorEnvelope envelope) {
        if (envelope == null || envelope.error() == null || envelope.error().details() == null) {
            return null;
        }
        for (Map<String, Object> detail : envelope.error().details()) {
            Object rawCode = detail.get("errorCode");
            if (rawCode instanceof String code && !code.isBlank()) {
                return code;
            }
        }
        return null;
    }

    private boolean looksLikeTokenValidation(String message, FcmErrorEnvelope envelope) {
        if (message != null && message.toLowerCase().contains("token")) {
            return true;
        }
        if (envelope == null || envelope.error() == null || envelope.error().details() == null) {
            return false;
        }
        for (Map<String, Object> detail : envelope.error().details()) {
            Object fieldViolations = detail.get("fieldViolations");
            if (fieldViolations instanceof List<?> violations) {
                for (Object violation : violations) {
                    if (violation instanceof Map<?, ?> violationMap) {
                        Object field = violationMap.get("field");
                        if (field instanceof String fieldName && fieldName.contains("message.token")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private record V1FcmRequest(V1Message message) {}

    private record V1Message(String token, Map<String, String> data, V1AndroidConfig android) {
        V1Message(String token, Map<String, String> data) {
            this(token, data == null ? new LinkedHashMap<>() : new LinkedHashMap<>(data), new V1AndroidConfig("HIGH"));
        }
    }

    private record V1AndroidConfig(String priority) {}

    private record FcmResponse(int statusCode, String body) {}

    private record FcmErrorEnvelope(FcmErrorBody error) {}

    private record FcmErrorBody(
            int code, String message, String status, @JsonProperty("details") List<Map<String, Object>> details) {}
}

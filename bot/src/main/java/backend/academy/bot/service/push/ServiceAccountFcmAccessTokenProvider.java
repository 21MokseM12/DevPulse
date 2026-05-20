package backend.academy.bot.service.push;

import backend.academy.bot.config.properties.PushProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ServiceAccountFcmAccessTokenProvider implements FcmAccessTokenProvider {
    private static final String FIREBASE_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final Duration TOKEN_SAFETY_WINDOW = Duration.ofSeconds(60);
    private static final String GOOGLE_APPLICATION_CREDENTIALS = "GOOGLE_APPLICATION_CREDENTIALS";

    private final PushProperties pushProperties;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final ReentrantLock refreshLock = new ReentrantLock();

    private volatile CachedToken cachedToken;

    @Autowired
    public ServiceAccountFcmAccessTokenProvider(
            PushProperties pushProperties,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Nullable Clock clock) {
        this.pushProperties = pushProperties;
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @Override
    public String getAccessToken() {
        Instant now = clock.instant();
        CachedToken current = cachedToken;
        if (current != null && current.isUsable(now)) {
            return current.value();
        }
        refreshLock.lock();
        try {
            now = clock.instant();
            current = cachedToken;
            if (current != null && current.isUsable(now)) {
                return current.value();
            }
            CachedToken refreshed = requestNewToken(now);
            cachedToken = refreshed;
            return refreshed.value();
        } finally {
            refreshLock.unlock();
        }
    }

    private CachedToken requestNewToken(Instant now) {
        ServiceAccountCredentials credentials = readCredentials();
        String assertion = buildSignedAssertion(credentials, now);
        String formBody = "grant_type="
                + urlEncode("urn:ietf:params:oauth:grant-type:jwt-bearer")
                + "&assertion="
                + urlEncode(assertion);

        String responseBody = webClientBuilder
                .build()
                .post()
                .uri(credentials.tokenUri())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue(formBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("Received empty OAuth response for Firebase access token");
        }
        try {
            OAuthTokenResponse response = objectMapper.readValue(responseBody, OAuthTokenResponse.class);
            if (response.accessToken() == null || response.accessToken().isBlank() || response.expiresIn() <= 0) {
                throw new IllegalStateException("OAuth response does not contain valid access token");
            }
            Instant expiresAt = now.plusSeconds(response.expiresIn());
            return new CachedToken(response.accessToken(), expiresAt.minus(TOKEN_SAFETY_WINDOW));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse OAuth token response", ex);
        }
    }

    private ServiceAccountCredentials readCredentials() {
        String configuredPath = pushProperties.fcm().credentialsPath();
        String credentialsPath = configuredPath == null || configuredPath.isBlank()
                ? System.getenv(GOOGLE_APPLICATION_CREDENTIALS)
                : configuredPath;
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalStateException(
                    "Missing service account credentials path: set BOT_FCM_CREDENTIALS_PATH or GOOGLE_APPLICATION_CREDENTIALS");
        }
        try {
            String json = Files.readString(Path.of(credentialsPath));
            ServiceAccountCredentials credentials = objectMapper.readValue(json, ServiceAccountCredentials.class);
            if (credentials.clientEmail() == null
                    || credentials.clientEmail().isBlank()
                    || credentials.privateKey() == null
                    || credentials.privateKey().isBlank()) {
                throw new IllegalStateException("Service account JSON misses client_email or private_key");
            }
            String tokenUri =
                    credentials.tokenUri() == null || credentials.tokenUri().isBlank()
                            ? "https://oauth2.googleapis.com/token"
                            : credentials.tokenUri();
            return new ServiceAccountCredentials(credentials.clientEmail(), credentials.privateKey(), tokenUri);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read service account JSON: " + credentialsPath, ex);
        }
    }

    private String buildSignedAssertion(ServiceAccountCredentials credentials, Instant now) {
        long issuedAt = now.getEpochSecond();
        Map<String, Object> header = Map.of("alg", "RS256", "typ", "JWT");
        Map<String, Object> payload = Map.of(
                "iss",
                credentials.clientEmail(),
                "scope",
                FIREBASE_SCOPE,
                "aud",
                credentials.tokenUri(),
                "iat",
                issuedAt,
                "exp",
                issuedAt + 3600);
        try {
            String encodedHeader = base64Url(objectMapper.writeValueAsBytes(header));
            String encodedPayload = base64Url(objectMapper.writeValueAsBytes(payload));
            String unsignedJwt = encodedHeader + "." + encodedPayload;
            String signature = sign(unsignedJwt, credentials.privateKey());
            return unsignedJwt + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign service account JWT assertion", ex);
        }
    }

    private String sign(String input, String privateKeyPem) throws Exception {
        String sanitized = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(sanitized);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(input.getBytes(StandardCharsets.UTF_8));
        return base64Url(signature.sign());
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record ServiceAccountCredentials(
            @JsonProperty("client_email") String clientEmail,
            @JsonProperty("private_key") String privateKey,
            @JsonProperty("token_uri") String tokenUri) {}

    private record OAuthTokenResponse(
            @JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") long expiresIn) {}

    private record CachedToken(String value, Instant usableUntil) {
        boolean isUsable(Instant now) {
            return now.isBefore(usableUntil);
        }
    }
}

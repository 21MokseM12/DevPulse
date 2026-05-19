package backend.academy.bot.model.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PushTokenUpsertRequest(
        @NotBlank(message = "platform is required")
                @Pattern(regexp = "(?i)^android$", message = "platform must be android")
                String platform,
        @NotBlank(message = "token is required")
                @Size(min = 16, max = 4096, message = "token length must be between 16 and 4096")
                String token,
        @Size(max = 64, message = "appVersion max length is 64") String appVersion,
        @Size(max = 255, message = "deviceId max length is 255") String deviceId) {}

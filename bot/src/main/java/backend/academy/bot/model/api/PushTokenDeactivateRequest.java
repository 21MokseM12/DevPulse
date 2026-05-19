package backend.academy.bot.model.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PushTokenDeactivateRequest(
        @NotBlank(message = "platform is required")
                @Pattern(regexp = "(?i)^android$", message = "platform must be android")
                String platform,
        @NotBlank(message = "token is required")
                @Size(min = 16, max = 4096, message = "token length must be between 16 and 4096")
                String token) {}

package backend.academy.bot.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityConfigTest {

    private final PasswordEncoder passwordEncoder = new SecurityConfig().passwordEncoder();

    @Test
    void passwordEncoder_hashesAndMatchesRawPassword() {
        String encoded = passwordEncoder.encode("pass-123");

        assertThat(encoded).isNotEqualTo("pass-123");
        assertThat(passwordEncoder.matches("pass-123", encoded)).isTrue();
        assertThat(passwordEncoder.matches("wrong-pass", encoded)).isFalse();
    }
}

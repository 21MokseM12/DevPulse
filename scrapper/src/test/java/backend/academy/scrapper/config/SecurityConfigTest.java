package backend.academy.scrapper.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityConfigTest {

    private final PasswordEncoder passwordEncoder = new SecurityConfig().passwordEncoder();

    @Test
    void passwordEncoder_hashesAndMatchesRawPassword() {
        String encoded = passwordEncoder.encode("secret");

        assertThat(encoded).isNotEqualTo("secret");
        assertThat(passwordEncoder.matches("secret", encoded)).isTrue();
        assertThat(passwordEncoder.matches("secret2", encoded)).isFalse();
    }
}

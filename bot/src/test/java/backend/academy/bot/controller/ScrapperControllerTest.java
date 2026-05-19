package backend.academy.bot.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.bot.config.ApplicationConfig;
import backend.academy.bot.service.notifications.LinkUpdateProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.LinkUpdate;

@WebMvcTest(controllers = ScrapperController.class)
public class ScrapperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinkUpdateProcessingService linkUpdateProcessingService;

    @MockitoBean
    private ApplicationConfig applicationConfig;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mapper.registerModule(new JavaTimeModule());
        when(applicationConfig.internalHeader()).thenReturn("X-Internal-Secret");
        when(applicationConfig.sharedSecret()).thenReturn("test-secret");
    }

    @Test
    public void testGetUpdatedLinkSuccess() throws Exception {
        LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                URI.create("https://example.com/resource"),
                URI.create("https://example.com/resource/event"),
                "title",
                "owner",
                "description",
                OffsetDateTime.of(LocalDate.of(2025, 3, 27), LocalTime.of(6, 50, 0), ZoneOffset.UTC),
                List.of("u1", "u2"));

        mockMvc.perform(post("/updates")
                        .header("X-Internal-Secret", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isOk());

        verify(linkUpdateProcessingService).process(linkUpdate);
    }

    @Test
    public void testGetUpdatedLinkBadRequestExceptionViaEmptyIdsList() throws Exception {
        LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                URI.create("https://example.com/resource"),
                URI.create("https://example.com/resource/event"),
                "title",
                "owner",
                "description",
                OffsetDateTime.of(LocalDate.of(2025, 3, 27), LocalTime.of(6, 50, 0), ZoneOffset.UTC),
                List.of());
        doThrow(new BadRequestException("Некорректные параметры запроса"))
                .when(linkUpdateProcessingService)
                .process(linkUpdate);

        mockMvc.perform(post("/updates")
                        .header("X-Internal-Secret", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isBadRequest());

        verify(linkUpdateProcessingService, times(1)).process(linkUpdate);
    }

    @Test
    public void testGetUpdatedLinkBadRequestExceptionViaLinkISNull() throws Exception {
        LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                null,
                null,
                "title",
                "owner",
                "description",
                OffsetDateTime.of(LocalDate.of(2025, 3, 27), LocalTime.of(6, 50, 0), ZoneOffset.UTC),
                List.of("u1", "u2"));
        mockMvc.perform(post("/updates")
                        .header("X-Internal-Secret", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isBadRequest());
        verify(linkUpdateProcessingService, times(0)).process(linkUpdate);
    }

    @Test
    public void notifyLinkUpdate_whenHeaderMissing_shouldReturnUnauthorized() throws Exception {
        LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                URI.create("https://example.com/resource"),
                URI.create("https://example.com/resource/event"),
                "title",
                "owner",
                "description",
                OffsetDateTime.of(LocalDate.of(2025, 3, 27), LocalTime.of(6, 50, 0), ZoneOffset.UTC),
                List.of("u1", "u2"));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.description").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.exceptionName").value("MissingInternalAuthHeaderException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Missing internal auth header"))
                .andExpect(jsonPath("$.stacktrace").isArray());

        verify(linkUpdateProcessingService, never()).process(linkUpdate);
    }

    @Test
    public void notifyLinkUpdate_whenHeaderInvalid_shouldReturnForbidden() throws Exception {
        LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                URI.create("https://example.com/resource"),
                URI.create("https://example.com/resource/event"),
                "title",
                "owner",
                "description",
                OffsetDateTime.of(LocalDate.of(2025, 3, 27), LocalTime.of(6, 50, 0), ZoneOffset.UTC),
                List.of("u1", "u2"));

        mockMvc.perform(post("/updates")
                        .header("X-Internal-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(linkUpdate)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.description").value("Forbidden"))
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.exceptionName").value("InvalidInternalAuthSecretException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Invalid internal auth secret"))
                .andExpect(jsonPath("$.stacktrace").isArray());

        verify(linkUpdateProcessingService, never()).process(linkUpdate);
    }
}

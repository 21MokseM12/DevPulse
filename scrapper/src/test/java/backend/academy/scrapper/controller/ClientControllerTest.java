package backend.academy.scrapper.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.service.ChatOperationProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(controllers = ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatOperationProcessor chatOperationProcessor;

    @Test
    void post_clients_registersClient() throws Exception {
        when(chatOperationProcessor.register(eq("alice"), eq("secret"))).thenReturn(true);

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"alice\",\"password\":\"secret\"}"))
                .andExpect(status().isOk());

        verify(chatOperationProcessor).register("alice", "secret");
    }

    @Test
    void post_clients_returns400WhenLoginTaken() throws Exception {
        when(chatOperationProcessor.register(eq("alice"), eq("secret"))).thenReturn(false);

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"alice\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_clients_unregistersClient() throws Exception {
        when(chatOperationProcessor.unregister(eq("alice"), eq("secret"))).thenReturn(true);

        mockMvc.perform(delete("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"alice\",\"password\":\"secret\"}"))
                .andExpect(status().isOk());

        verify(chatOperationProcessor).unregister("alice", "secret");
    }

    @Test
    void delete_clients_returns404WhenClientMissing() throws Exception {
        when(chatOperationProcessor.unregister(eq("alice"), eq("secret"))).thenReturn(false);

        mockMvc.perform(delete("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"alice\",\"password\":\"secret\"}"))
                .andExpect(status().isNotFound());
    }
}

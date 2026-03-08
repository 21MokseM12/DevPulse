package backend.academy.scrapper.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.service.ChatOperationProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ChatController.class)
public class ChatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ChatOperationProcessor chatOperationProcessor;

    @Test
    public void testRegisterChatSuccessfully() throws Exception {
        Long id = 123L;
        when(chatOperationProcessor.register(id)).thenReturn(true);

        mockMvc.perform(post("/tg-chat/{id}", id)).andExpect(status().isOk());

        verify(chatOperationProcessor, times(1)).register(id);
    }

    @Test
    public void testRegisterChatBadRequestException() throws Exception {
        Long id = -1L;
        mockMvc.perform(post("/tg-chat/{id}", id)).andExpect(status().isBadRequest());

        verify(chatOperationProcessor, times(0)).register(id);
    }

    @Test
    public void testUnregisterChatSuccessfully() throws Exception {
        Long id = 123L;
        when(chatOperationProcessor.unregister(id)).thenReturn(true);

        mockMvc.perform(delete("/tg-chat/{id}", id)).andExpect(status().isOk());

        verify(chatOperationProcessor, times(1)).unregister(id);
    }

    @Test
    public void testUnregisterChatBadRequestException() throws Exception {
        Long id = -1L;
        mockMvc.perform(delete("/tg-chat/{id}", id)).andExpect(status().isBadRequest());

        verify(chatOperationProcessor, times(0)).unregister(id);
    }

    @Test
    public void testUnregisterChatResourceNotFoundException() throws Exception {
        Long id = 123L;
        when(chatOperationProcessor.unregister(id)).thenReturn(false);
        mockMvc.perform(delete("/tg-chat/{id}", id)).andExpect(status().isNotFound());

        verify(chatOperationProcessor, times(1)).unregister(id);
    }
}

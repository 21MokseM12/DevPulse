package backend.academy.scrapper.database.orm;

import backend.academy.scrapper.database.orm.repository.OrmChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrmChatServiceTest {

    @Mock
    private OrmChatRepository ormChatRepository;

    @InjectMocks
    private OrmChatService ormChatService;

    @Test
    public void register_whenIdExists_shouldReturnFalse() {
        Long id = 1L;
        when(ormChatRepository.existsById(id)).thenReturn(true);

        boolean registered = ormChatService.register(id);
        assertFalse(registered);
        verify(ormChatRepository, times(0)).save(any());
    }

    @Test
    public void register_whenIdIsNotExists_shouldReturnTrue() {
        Long id = 1L;
        when(ormChatRepository.existsById(id)).thenReturn(false);

        boolean registered = ormChatService.register(id);
        assertTrue(registered);
        verify(ormChatRepository).save(any());
    }

    @Test
    public void unregister_whenIdExist_shouldReturnTrue() {
        Long id = 1L;
        when(ormChatRepository.existsById(id)).thenReturn(true);

        boolean registered = ormChatService.unregister(id);
        assertTrue(registered);
        verify(ormChatRepository).deleteById(id);
    }

    @Test
    public void unregister_whenIdIsNotExist_shouldReturnFalse() {
        Long id = 1L;
        when(ormChatRepository.existsById(id)).thenReturn(false);

        boolean registered = ormChatService.unregister(id);
        assertFalse(registered);
        verify(ormChatRepository, times(0)).deleteById(id);
    }
}

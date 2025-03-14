package backend.academy.scrapper.database.orm;

import backend.academy.scrapper.database.LinkService;
import backend.academy.scrapper.database.orm.entity.ChatEntity;
import backend.academy.scrapper.database.orm.entity.LinkEntity;
import backend.academy.scrapper.database.orm.mapper.LinkMapper;
import backend.academy.scrapper.database.orm.repository.OrmChatRepository;
import backend.academy.scrapper.database.orm.repository.OrmLinkRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Service
public class OrmLinkService implements LinkService {

    private final OrmChatRepository ormChatRepository;

    private final OrmLinkRepository ormLinkRepository;

    @Autowired
    public OrmLinkService(OrmLinkRepository ormLinkRepository, OrmChatRepository ormChatRepository) {
        this.ormChatRepository = ormChatRepository;
        this.ormLinkRepository = ormLinkRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<LinkResponse> findAllByChatId(Long chatId) {
        if (!ormChatRepository.existsById(chatId)) {
            return new ArrayList<>();
        }

        return ormChatRepository.findAllById(chatId).stream()
                .map(LinkMapper::map)
                .toList();
    }

    @Transactional
    @Override
    public Optional<LinkResponse> subscribe(Long chatId, AddLinkRequest link) {
        Optional<ChatEntity> chat = ormChatRepository.findById(chatId);
        if (chat.isEmpty()) {
            return Optional.empty();
        }
        LinkEntity linkResponse = ormLinkRepository
                .findByLink(link.link().toString())
                .orElseGet(() -> {
                    LinkEntity linkEntity = LinkMapper.map(link);
                    return ormLinkRepository.save(linkEntity);
                });
        chat.get().links().add(linkResponse);
        return Optional.of(LinkMapper.map(linkResponse));
    }

    @Transactional
    @Override
    public Optional<LinkResponse> unsubscribe(Long chatId, RemoveLinkRequest removeLinkRequest) {
        Optional<ChatEntity> chat = ormChatRepository.findById(chatId);
        if (chat.isEmpty()) {
            return Optional.empty();
        }
        Optional<LinkEntity> linkEntity =
                ormLinkRepository.findByLink(removeLinkRequest.link().toString());
        if (linkEntity.isEmpty()) {
            return Optional.empty();
        }
        if (!chat.get().links().contains(linkEntity.get())) {
            return Optional.empty();
        }
        chat.get().links().remove(linkEntity.get());
        return Optional.of(LinkMapper.map(linkEntity.get()));
    }
}

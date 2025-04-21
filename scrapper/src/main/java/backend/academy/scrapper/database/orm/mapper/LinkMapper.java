package backend.academy.scrapper.database.orm.mapper;

import backend.academy.scrapper.database.orm.entity.ChatEntity;
import backend.academy.scrapper.database.orm.entity.FilterEntity;
import backend.academy.scrapper.database.orm.entity.LinkEntity;
import backend.academy.scrapper.database.orm.entity.TagEntity;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

public class LinkMapper {

    public static LinkResponse map(LinkEntity linkEntity) {
        return new LinkResponse(
                linkEntity.id(),
                URI.create(linkEntity.link()),
                linkEntity.tags().stream().map(TagEntity::tag).collect(Collectors.toSet()),
                linkEntity.filters().stream().map(FilterEntity::filter).collect(Collectors.toSet()));
    }

    public static LinkEntity map(
            LinkEntity linkEntity,
            AddLinkRequest link,
            Set<TagEntity> tagEntities,
            Set<FilterEntity> filterEntities,
            ChatEntity chatEntity) {
        linkEntity.link(link.link().toString());
        linkEntity.tags(tagEntities);
        linkEntity.filters(filterEntities);
        linkEntity.chats(new HashSet<>());
        linkEntity.chats().add(chatEntity);
        return linkEntity;
    }
}

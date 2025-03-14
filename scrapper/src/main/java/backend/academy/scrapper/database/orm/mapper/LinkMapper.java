package backend.academy.scrapper.database.orm.mapper;

import backend.academy.scrapper.database.orm.entity.FilterEntity;
import backend.academy.scrapper.database.orm.entity.LinkEntity;
import backend.academy.scrapper.database.orm.entity.TagEntity;
import java.net.URI;
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

    public static LinkEntity map(AddLinkRequest addLinkRequest) {
        LinkEntity linkEntity = new LinkEntity();
        linkEntity.link(addLinkRequest.link().toString());
        linkEntity.tags(addLinkRequest.tags().stream().map(TagEntity::new).collect(Collectors.toSet()));
        linkEntity.filters(
                addLinkRequest.filters().stream().map(FilterEntity::new).collect(Collectors.toSet()));
        return linkEntity;
    }
}

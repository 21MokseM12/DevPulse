package backend.academy.scrapper.mapper;

import backend.academy.scrapper.db.model.Link;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import java.time.OffsetDateTime;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LinkMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "request.link", target = "url")
    @Mapping(source = "request.tags", target = "tags")
    @Mapping(source = "request.filters", target = "filters")
    @Mapping(source = "createdTime", target = "createdAt")
    Link toLink(AddLinkRequest request, Long id, OffsetDateTime createdTime);

    @Mapping(source = "link.id", target = "id")
    @Mapping(source = "link.url", target = "url")
    @Mapping(source = "link.createdAt", target = "createdAt")
    @Mapping(source = "link.tags", target = "tags")
    @Mapping(source = "link.filters", target = "filters")
    Link toLink(Link link, Set<String> tags, Set<String> filters);
}

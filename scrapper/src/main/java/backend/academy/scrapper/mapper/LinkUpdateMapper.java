package backend.academy.scrapper.mapper;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.NotifyUpdateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import scrapper.bot.connectivity.model.LinkUpdate;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LinkUpdateMapper {

    @Mapping(source = "update.id", target = "id")
    @Mapping(source = "notification.link", target = "url")
    @Mapping(source = "update.title", target = "title")
    @Mapping(source = "update.updateOwner", target = "updateOwner")
    @Mapping(source = "update.descriptionPreview", target = "description")
    @Mapping(source = "update.creationDate", target = "creationDate")
    @Mapping(source = "notification.chatIds", target = "clientsIds")
    LinkUpdate toLinkUpdate(LinkUpdateDTO update, NotifyUpdateEntity notification);
}

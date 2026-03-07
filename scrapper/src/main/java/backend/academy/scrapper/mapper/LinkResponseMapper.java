package backend.academy.scrapper.mapper;

import backend.academy.scrapper.db.model.Link;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LinkResponseMapper {

    LinkResponse toLinkResponse(Link link);
}

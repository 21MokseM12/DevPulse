package scrapper.bot.connectivity.model.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.Set;
import scrapper.bot.connectivity.serialization.UniqueStringSetDeserializer;
import scrapper.bot.connectivity.validation.ValidUri;

public record AddLinkRequest(
        @NotNull @ValidUri URI link,
        @JsonDeserialize(using = UniqueStringSetDeserializer.class) Set<String> tags,
        @JsonDeserialize(using = UniqueStringSetDeserializer.class) Set<String> filters) {}

package backend.academy.scrapper.model;

import java.net.URI;
import java.util.List;

public record NotifyUpdateEntity(URI link, List<LinkUpdateDTO> updates, List<Long> chatIds) {}

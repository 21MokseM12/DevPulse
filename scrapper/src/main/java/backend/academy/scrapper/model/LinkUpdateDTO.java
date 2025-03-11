package backend.academy.scrapper.model;

import java.net.URI;

public record LinkUpdateDTO(Long id, URI url, String description) {}

package backend.academy.scrapper.model.kafka;

import backend.academy.scrapper.enums.actions.ClientActions;

public record ClientMessage(
    ClientActions action,
    Long id
) { }

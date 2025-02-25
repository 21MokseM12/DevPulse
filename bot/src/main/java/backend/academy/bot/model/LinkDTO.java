package backend.academy.bot.model;

import backend.academy.bot.enums.TrackCommandStates;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class LinkDTO {

    private TrackCommandStates state;

    private String link;

    private List<String> tags;

    private List<String> filters;

    public LinkDTO(TrackCommandStates state) {
        this.state = state;
    }
}

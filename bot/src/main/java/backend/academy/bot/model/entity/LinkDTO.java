package backend.academy.bot.model.entity;

import backend.academy.bot.enums.TrackCommandStates;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class LinkDTO {

    private TrackCommandStates state;

    private String uri;

    private List<String> tags;

    private List<String> filters;

    public LinkDTO(TrackCommandStates state) {
        this.state = state;
    }
}

package scrapper.bot.connectivity.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Link {

    private Integer id;

    private String uri;

    private List<String> tags;

    private List<String> filters;

    public Link(Integer id, String uri) {
        this.id = id;
        this.uri = uri;
    }
}

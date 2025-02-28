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
public class LinkRequest {

    private String uri;

    private List<String> tags;

    private List<String> filters;

    public LinkRequest(String uri) {
        this.uri = uri;
    }
}

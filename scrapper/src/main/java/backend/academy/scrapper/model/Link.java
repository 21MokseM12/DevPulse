package backend.academy.scrapper.model;

import java.net.URI;
import java.time.OffsetDateTime;
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

    private Long id;

    private URI url;

    private List<String> tags;

    private List<String> filters;

    private OffsetDateTime createdAt;
}

package backend.academy.scrapper.database.model;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;
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

    private Set<String> tags;

    private Set<String> filters;

    private OffsetDateTime createdAt;
}

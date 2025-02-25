package backend.academy.bot.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class Link implements Serializable {

    private int id;

    private String link;
}

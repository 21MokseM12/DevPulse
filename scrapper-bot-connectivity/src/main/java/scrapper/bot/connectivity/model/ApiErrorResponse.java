package scrapper.bot.connectivity.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class ApiErrorResponse implements Serializable {

    private String description;

    private String code;

    private String exceptionName;

    private String exceptionMessage;

    private List<String> stackTrace;
}

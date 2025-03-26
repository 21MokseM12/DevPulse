package backend.academy.scrapper.service.updaters.processors;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.stackoverflow.StackOverflowQuestionItem;
import java.net.URI;
import java.util.List;

public interface StackOverflowQuestionUpdateProcessor {
    List<LinkUpdateDTO> processUpdates(URI link, Long questionId, StackOverflowQuestionItem question);
}

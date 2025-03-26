package backend.academy.scrapper.service.updaters.processors.impl;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.enums.ProcessedIdType;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.stackoverflow.ProcessedIdDTO;
import backend.academy.scrapper.model.stackoverflow.StackOverflowAnswerItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowQuestionItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowResponse;
import backend.academy.scrapper.model.stackoverflow.mappers.StackOverflowResponseMapper;
import backend.academy.scrapper.service.updaters.links.wrappers.impl.StackOverflowLinkService;
import backend.academy.scrapper.service.updaters.processors.StackOverflowQuestionUpdateProcessor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowAnswerUpdateProcessor implements StackOverflowQuestionUpdateProcessor {

    private final StackOverflowLinkService linkService;

    private final StackOverflowClient stackOverflowClient;

    @Autowired
    public StackOverflowAnswerUpdateProcessor(
            StackOverflowLinkService linkService, StackOverflowClient stackOverflowClient) {
        this.linkService = linkService;
        this.stackOverflowClient = stackOverflowClient;
    }

    @Override
    public List<LinkUpdateDTO> processUpdates(URI link, Long questionId, StackOverflowQuestionItem question) {
        List<LinkUpdateDTO> resultUpdatesList = new ArrayList<>();
        List<ProcessedIdDTO> nowProcessedIds = new ArrayList<>();

        List<Long> alreadyProcessedAnswersIds = linkService.getProcessedAnswersIds(link);
        ResponseEntity<StackOverflowResponse<StackOverflowAnswerItem>> answersResponse =
                stackOverflowClient.getAnswersByQuestionId(questionId, "stackoverflow", "withbody");
        if (answersResponse.getStatusCode() == HttpStatus.OK) {
            var answers = Objects.requireNonNull(answersResponse.getBody());
            answers.items().stream()
                    .filter(answer -> !alreadyProcessedAnswersIds.contains(answer.id()))
                    .forEach(answer -> {
                        resultUpdatesList.add(StackOverflowResponseMapper.mapToAnswer(answer, question));
                        nowProcessedIds.add(new ProcessedIdDTO(answer.id(), ProcessedIdType.STACKOVERFLOW_ANSWER));
                    });
            linkService.saveProcessedIds(link, nowProcessedIds);
        }

        return resultUpdatesList;
    }
}

package backend.academy.scrapper.model.stackoverflow.mappers;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.UpdateType;
import backend.academy.scrapper.model.stackoverflow.StackOverflowAnswerItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowCommentItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowQuestionItem;
import java.net.URI;
import java.util.Set;

public class StackOverflowResponseMapper {

    private static final int bodyLength = 200;

    public static LinkUpdateDTO mapToAnswer(
            StackOverflowAnswerItem answer, StackOverflowQuestionItem question, Long questionId) {
        String body =
                answer.answer().length() > bodyLength ? answer.answer().substring(bodyLength + 1) : answer.answer();
        return new LinkUpdateDTO(
                answer.id(),
                question.title(),
                answer.owner().username(),
                answer.creationDate(),
                body,
                UpdateType.STACKOVERFLOW_ANSWER,
                questionTags(question),
                URI.create("https://stackoverflow.com/questions/" + questionId + "/#answer-" + answer.id()));
    }

    public static LinkUpdateDTO mapToComment(
            StackOverflowCommentItem comment, StackOverflowQuestionItem question, Long questionId) {
        String body = comment.comment().length() > bodyLength
                ? comment.comment().substring(bodyLength + 1)
                : comment.comment();
        URI eventUrl = URI.create(
                "https://stackoverflow.com/questions/" + questionId + "#comment" + comment.id() + "_" + questionId);
        return new LinkUpdateDTO(
                comment.id(),
                question.title(),
                comment.owner().username(),
                comment.creationDate(),
                body,
                UpdateType.STACKOVERFLOW_COMMENT,
                questionTags(question),
                eventUrl);
    }

    private static Set<String> questionTags(StackOverflowQuestionItem question) {
        return question.tags() == null ? Set.of() : Set.copyOf(question.tags());
    }
}

package backend.academy.scrapper.model.stackoverflow.mappers;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.stackoverflow.StackOverflowAnswerItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowCommentItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowQuestionItem;

public class StackOverflowResponseMapper {

    private static final int bodyLength = 200;

    public static LinkUpdateDTO mapToAnswer(StackOverflowAnswerItem answer, StackOverflowQuestionItem question) {
        String body =
                answer.answer().length() > bodyLength ? answer.answer().substring(bodyLength + 1) : answer.answer();
        return new LinkUpdateDTO(answer.id(), question.title(), answer.owner().username(), answer.creationDate(), body);
    }

    public static LinkUpdateDTO mapToComment(StackOverflowCommentItem comment, StackOverflowQuestionItem question) {
        String body = comment.comment().length() > bodyLength
                ? comment.comment().substring(bodyLength + 1)
                : comment.comment();
        return new LinkUpdateDTO(
                comment.id(), question.title(), comment.owner().username(), comment.creationDate(), body);
    }
}

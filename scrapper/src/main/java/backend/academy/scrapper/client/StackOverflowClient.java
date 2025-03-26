package backend.academy.scrapper.client;

import backend.academy.scrapper.model.stackoverflow.StackOverflowAnswerItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowCommentItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowQuestionItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/questions/{questionId}")
public interface StackOverflowClient {

    @GetExchange
    ResponseEntity<StackOverflowResponse<StackOverflowQuestionItem>> getQuestionById(
        @PathVariable Long questionId,
        @RequestParam String site
    );

    @GetExchange("/answers")
    ResponseEntity<StackOverflowResponse<StackOverflowAnswerItem>> getAnswersByQuestionId(
        @PathVariable Long questionId,
        @RequestParam String site,
        @RequestParam String filter
    );

    @GetExchange("/comments")
    ResponseEntity<StackOverflowResponse<StackOverflowCommentItem>> getCommentsByQuestionId(
        @PathVariable Long questionId,
        @RequestParam String site,
        @RequestParam String filter
    );
}

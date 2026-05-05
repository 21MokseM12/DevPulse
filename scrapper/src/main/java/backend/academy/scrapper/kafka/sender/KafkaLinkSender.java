package backend.academy.scrapper.kafka.sender;

import java.util.List;
import scrapper.bot.connectivity.model.response.LinkResponse;

public interface KafkaLinkSender {
    void send(List<LinkResponse> response);
}

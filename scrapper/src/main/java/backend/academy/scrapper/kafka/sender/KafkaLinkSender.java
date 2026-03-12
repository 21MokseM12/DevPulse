package backend.academy.scrapper.kafka.sender;

import scrapper.bot.connectivity.model.response.LinkResponse;
import java.util.List;

public interface KafkaLinkSender {
    void send(List<LinkResponse> response);
}

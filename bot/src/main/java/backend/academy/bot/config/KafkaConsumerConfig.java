package backend.academy.bot.config;

import backend.academy.bot.config.properties.BotKafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
@EnableKafka
@EnableConfigurationProperties(BotKafkaProperties.class)
public class KafkaConsumerConfig {

    public static final String LINK_UPDATES_LISTENER_CONTAINER_FACTORY = "linkUpdatesListenerContainerFactory";

    @Bean(LINK_UPDATES_LISTENER_CONTAINER_FACTORY)
    public ConcurrentKafkaListenerContainerFactory<String, String> linkUpdatesListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory, BotKafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        int maxRetries = Math.max(0, kafkaProperties.retryPolicy().maxAttempts() - 1);
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(maxRetries);
        backOff.setInitialInterval(kafkaProperties.retryPolicy().interval());
        backOff.setMultiplier(kafkaProperties.retryPolicy().multiplier());
        backOff.setMaxInterval(kafkaProperties.retryPolicy().maxDelay());
        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));
        return factory;
    }
}

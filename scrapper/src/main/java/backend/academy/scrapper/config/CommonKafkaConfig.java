package backend.academy.scrapper.config;

import backend.academy.scrapper.config.properties.CommonKafkaProperties;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableConfigurationProperties(CommonKafkaProperties.class)
public class CommonKafkaConfig {

    public static final String COMMON_CONSUMER_FACTORY = "commonKafkaConsumerFactory";
    public static final String COMMON_CONTAINER_FACTORY = "commonKafkaContainerFactory";
    public static final String STRING_VALUE_CONSUMER_FACTORY = "stringValueKafkaConsumerFactory";
    public static final String STRING_VALUE_CONTAINER_FACTORY = "stringValueKafkaContainerFactory";
    public static final String COMMON_PRODUCER_FACTORY = "commonKafkaProducerFactory";
    public static final String COMMON_KAFKA_TEMPLATE = "commonKafkaTemplate";

    @Bean(COMMON_CONSUMER_FACTORY)
    public ConsumerFactory<String, Object> consumerFactory(CommonKafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.consumer().autoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.consumer().enableAutoCommit());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages(kafkaProperties.consumer().trustedPackages());
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    @Bean(COMMON_CONTAINER_FACTORY)
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
        @Qualifier(COMMON_CONSUMER_FACTORY) ConsumerFactory<String, Object> consumerFactory,
        CommonKafkaProperties kafkaProperties
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory
            = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(kafkaProperties.consumer().ackMode());
        return factory;
    }

    @Bean(STRING_VALUE_CONSUMER_FACTORY)
    public ConsumerFactory<String, String> stringValueConsumerFactory(
        CommonKafkaProperties kafkaProperties
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.consumer().autoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.consumer().enableAutoCommit());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(STRING_VALUE_CONTAINER_FACTORY)
    public ConcurrentKafkaListenerContainerFactory<String, String> stringValueContainerFactory(
        @Qualifier(STRING_VALUE_CONSUMER_FACTORY) ConsumerFactory<String, String> consumerFactory,
        CommonKafkaProperties kafkaProperties
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory
            = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(kafkaProperties.consumer().ackMode());
        return factory;
    }

    @Bean(COMMON_PRODUCER_FACTORY)
    public ProducerFactory<String, Object> producerFactory(CommonKafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProperties.producer().clientId());

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.producer().acksConfig());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, kafkaProperties.producer().enableIdempotenceConfig());

        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>();
        jsonSerializer.setAddTypeInfo(false);

        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), jsonSerializer);
    }

    @Bean(COMMON_KAFKA_TEMPLATE)
    public KafkaTemplate<String, Object> kafkaTemplate(
        @Qualifier(COMMON_PRODUCER_FACTORY) ProducerFactory<String, Object> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}

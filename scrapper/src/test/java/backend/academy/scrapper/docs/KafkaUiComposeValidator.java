package backend.academy.scrapper.docs;

final class KafkaUiComposeValidator {

    private KafkaUiComposeValidator() {}

    static boolean hasKafkaUiService(String compose) {
        return compose.contains("kafka-ui:")
                && compose.contains("image: provectuslabs/kafka-ui:latest")
                && compose.contains("- \"8082:8080\"");
    }

    static boolean hasKafkaUiKafkaConnection(String compose) {
        return compose.contains("KAFKA_CLUSTERS_0_NAME=local")
                && compose.contains("KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:9092");
    }
}

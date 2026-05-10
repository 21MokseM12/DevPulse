package backend.academy.scrapper.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class ScrapperLoggingConfigurationIntegrationTest {

    @Test
    void prodConfiguration_writesJsonWithTracingFieldsAndMaskedSecrets() throws Exception {
        LoggerContext context = configureContext("logback-prod.xml");
        ListAppender<ILoggingEvent> capture = new ListAppender<>();
        try {
            Logger logger = context.getLogger(ScrapperLoggingConfigurationIntegrationTest.class);
            capture.start();
            logger.addAppender(capture);
            MDC.put("traceId", "trace-501");
            MDC.put("spanId", "span-601");
            MDC.put("requestId", "req-701");

            logger.info("secret top-secret polling done");

            ILoggingEvent event = capture.list.stream().findFirst().orElseThrow();
            SecretMaskingConverter maskingConverter = new SecretMaskingConverter();
            String maskedMessage = maskingConverter.convert(event);
            Appender<ILoggingEvent> appender =
                    context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("JSON_CONSOLE");

            assertThat(appender).isNotNull();
            assertThat(event.getLevel().levelStr).isEqualTo("INFO");
            assertThat(maskedMessage).contains("secret ***");
        } finally {
            MDC.clear();
            context.stop();
        }
    }

    @Test
    void devConfiguration_staysHumanReadable() throws Exception {
        LoggerContext context = configureContext("logback-dev.xml");
        ListAppender<ILoggingEvent> capture = new ListAppender<>();
        try {
            Logger logger = context.getLogger(ScrapperLoggingConfigurationIntegrationTest.class);
            capture.start();
            logger.addAppender(capture);
            logger.info("token=abc123 scheduler heartbeat");
        } finally {
            // no-op
        }

        ILoggingEvent event = capture.list.stream().findFirst().orElseThrow();
        SecretMaskingConverter maskingConverter = new SecretMaskingConverter();
        String maskedMessage = maskingConverter.convert(event);
        Appender<ILoggingEvent> appender =
                context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("CONSOLE");

        assertThat(appender).isNotNull();
        assertThat(event.getLevel().levelStr).isEqualTo("INFO");
        assertThat(maskedMessage).contains("token=***");
        assertThat(maskedMessage).contains("scheduler heartbeat");
        assertThat(maskedMessage).doesNotStartWith("{");
        context.stop();
    }

    private LoggerContext configureContext(String resourcePath) throws JoranException {
        LoggerContext context = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        URL resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        assertThat(resource).isNotNull();
        configurator.doConfigure(resource);
        context.start();
        return context;
    }
}

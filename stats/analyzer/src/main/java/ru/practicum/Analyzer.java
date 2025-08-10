package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.processor.EventsSimilarityProcessor;
import ru.practicum.processor.UserActionProcessor;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Analyzer {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Analyzer.class, args);

        final UserActionProcessor userActionProcessor = context.getBean(UserActionProcessor.class);
        EventsSimilarityProcessor eventsSimilarityProcessor = context.getBean(EventsSimilarityProcessor.class);

        Thread hubEventsThread = new Thread(userActionProcessor);
        hubEventsThread.setName("UserActionHandlerThread");
        hubEventsThread.start();
        eventsSimilarityProcessor.start();
    }
}
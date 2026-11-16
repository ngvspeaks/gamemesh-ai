package com.gamemesh.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class RabbitMqConfig {

    @Value("${gamemesh.rabbitmq.exchange:gamemesh.telemetry.exchange}")
    private String telemetryExchange;

    @Value("${gamemesh.rabbitmq.queue:gamemesh.telemetry.queue}")
    private String telemetryQueue;

    @Value("${gamemesh.rabbitmq.routing-key:telemetry.event.#}")
    private String routingKey;

    @Value("${gamemesh.rabbitmq.dlx:gamemesh.telemetry.dlx}")
    private String deadLetterExchange;

    @Value("${gamemesh.rabbitmq.dlq:gamemesh.telemetry.dlq}")
    private String deadLetterQueue;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(telemetryExchange, true, false);
    }

    @Bean
    @Primary
    public Queue mainQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", deadLetterExchange);
        args.put("x-dead-letter-routing-key", deadLetterQueue);
        return new Queue(telemetryQueue, true, false, false, args);
    }

    @Bean
    public Binding mainBinding(@Qualifier("mainQueue") Queue mainQueue, TopicExchange exchange) {
        return BindingBuilder.bind(mainQueue).to(exchange).with(routingKey);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchange, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(deadLetterQueue, true);
    }

    @Bean
    public Binding deadLetterBinding(@Qualifier("deadLetterQueue") Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(deadLetterQueue.getName());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    /**
     * Listener container factory configured with:
     * - Auto-acknowledgment
     * - Multi-consumer concurrency (3-10 consumers)
     * - Exponential backoff retry policy (initial: 1s, multiplier: 2.0, max: 10s, maxAttempts: 3)
     * - RejectAndDontRequeueRecoverer: routes exhausted messages to DLQ (gamemesh.telemetry.dlq)
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(20);
        factory.setDefaultRequeueRejected(false); // Rejecting without requeue routes into DLX

        // 1. Exponential backoff retry template
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000L);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000L);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 2. Advise chain with retry and dead-letter recoverer
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .retryOperations(retryTemplate)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );

        // 3. Error handler for logging failed consumption attempts
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(new FatalExceptionStrategy() {
            @Override
            public boolean isFatal(Throwable t) {
                log.error("RabbitMQ Consumer error encountered. Detail: {}", t.getMessage());
                return false;
            }
        }));

        return factory;
    }
}

package org.dspace.log.appender;

import java.util.Arrays;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;

import javax.jms.ConnectionFactory;

@Configuration
@ComponentScan(basePackageClasses = { JMSListener.class })
@EnableJms
public class MessagingConfiguration {
    public static final String QUEUE_NAME = "testTopic";

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory();
        return connectionFactory;
    }

    @Bean
    public JmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory =
                new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        //core poll size=4 threads and max poll size 8 threads
        factory.setConcurrency("4-8");
        return factory;
    }


}